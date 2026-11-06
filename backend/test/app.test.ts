import request from "supertest";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createApp } from "../src/app";
import { EmailDeliveryError } from "../src/email/emailSender";
import { CatalogueService } from "../src/services/catalogueService";
import { EnquiryService } from "../src/services/enquiryService";
import { FakeEmailSender, FakeProductSource } from "./fakes";

let source: FakeProductSource;
let sender: FakeEmailSender;
let app: ReturnType<typeof createApp>;

beforeEach(() => {
  vi.spyOn(console, "error").mockImplementation(() => {});
  source = new FakeProductSource();
  sender = new FakeEmailSender();
  const catalogue = new CatalogueService(source, {
    featuredIds: [1, 2, 3, 4, 5],
    featuredCount: 5,
    categoryTopCount: 2,
  });
  app = createApp({
    catalogue,
    enquiries: new EnquiryService(catalogue, sender, "sales@acme.test", "no-reply@acme.test"),
  });
});

describe("home", () => {
  it("returns the 5 featured products", async () => {
    const res = await request(app).get("/api/home");
    expect(res.status).toBe(200);
    expect(res.body.featured.map((x: { id: number }) => x.id)).toEqual([1, 2, 3, 4, 5]);
  });

  it("returns 502 when product data is unavailable", async () => {
    source.failing = true;
    const res = await request(app).get("/api/home");
    expect(res.status).toBe(502);
    expect(res.body.error).toMatch(/unavailable/i);
  });
});

describe("categories", () => {
  it("lists categories", async () => {
    const res = await request(app).get("/api/categories");
    expect(res.body.categories).toEqual(["bags", "jewelery", "men's clothing"]);
  });

  it("returns top rated then additional products, handling URL-encoded names", async () => {
    const res = await request(app).get(`/api/categories/${encodeURIComponent("men's clothing")}`);
    expect(res.status).toBe(200);
    expect(res.body.category).toBe("men's clothing");
    expect(res.body.topRated).toHaveLength(2);
    expect(res.body.others).toHaveLength(0);
  });

  it("returns 404 for an unknown category", async () => {
    expect((await request(app).get("/api/categories/nope")).status).toBe(404);
  });
});

describe("search", () => {
  it("returns only matching products", async () => {
    const res = await request(app).get("/api/products").query({ q: "ring" });
    expect(res.status).toBe(200);
    expect(res.body.products.map((x: { id: number }) => x.id)).toEqual([5]);
  });

  it("rejects a missing or blank query", async () => {
    expect((await request(app).get("/api/products")).status).toBe(400);
    expect((await request(app).get("/api/products").query({ q: "  " })).status).toBe(400);
  });
});

describe("product details", () => {
  it("returns the product", async () => {
    const res = await request(app).get("/api/products/2");
    expect(res.status).toBe(200);
    expect(res.body.product.title).toBe("Red Backpack");
  });

  it("returns 404 for a missing product and 400 for a bad id", async () => {
    expect((await request(app).get("/api/products/999")).status).toBe(404);
    expect((await request(app).get("/api/products/abc")).status).toBe(400);
  });
});

describe("enquiries", () => {
  const valid = { productId: 2, name: "Asha Rao", email: "asha@example.com", phone: "+91 98765 43210", message: "Is it in stock?" };

  it("emails the sales inbox and returns 201", async () => {
    const res = await request(app).post("/api/enquiries").send(valid);
    expect(res.status).toBe(201);
    expect(res.body).toEqual({ ok: true });
    expect(sender.sent).toHaveLength(1);
    const mail = sender.sent[0]!;
    expect(mail.to).toBe("sales@acme.test");
    expect(mail.replyTo).toBe("asha@example.com");
    expect(mail.subject).toContain("Red Backpack");
    expect(mail.text).toContain("Asha Rao");
    expect(mail.text).toContain("Is it in stock?");
  });

  it("accepts an enquiry without phone or message", async () => {
    const res = await request(app)
      .post("/api/enquiries")
      .send({ productId: 2, name: "A", email: "a@b.co", phone: "" });
    expect(res.status).toBe(201);
    expect(sender.sent[0]!.text).toContain("Phone: not provided");
  });

  it("rejects invalid input without sending email", async () => {
    const bad = [
      { ...valid, email: "not-an-email" },
      { ...valid, name: "   " },
      { ...valid, phone: "abc" },
      { ...valid, productId: "2" },
      { ...valid, message: "x".repeat(1001) },
    ];
    for (const body of bad) {
      const res = await request(app).post("/api/enquiries").send(body);
      expect(res.status).toBe(400);
    }
    expect(sender.sent).toHaveLength(0);
  });

  it("returns 404 for an unknown product", async () => {
    const res = await request(app).post("/api/enquiries").send({ ...valid, productId: 999 });
    expect(res.status).toBe(404);
    expect(sender.sent).toHaveLength(0);
  });

  it("does not report success when the email fails to send", async () => {
    sender.error = new EmailDeliveryError("smtp down");
    const res = await request(app).post("/api/enquiries").send(valid);
    expect(res.status).toBe(503);
  });

  it("rejects malformed JSON with 400", async () => {
    const res = await request(app)
      .post("/api/enquiries")
      .set("Content-Type", "application/json")
      .send("{not json");
    expect(res.status).toBe(400);
  });

  it("keeps newlines in customer names out of the subject line", async () => {
    await request(app)
      .post("/api/enquiries")
      .send({ ...valid, name: "Evil\r\nBcc: attacker@example.com" });
    expect(sender.sent[0]!.subject).not.toMatch(/[\r\n]/);
  });
});
