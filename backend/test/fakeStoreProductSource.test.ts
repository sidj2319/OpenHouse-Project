import { describe, expect, it, vi } from "vitest";
import { FakeStoreProductSource } from "../src/providers/fakeStoreProductSource";
import { ProductSourceError } from "../src/providers/productSource";

const upstream = [
  {
    id: 1,
    title: "Backpack",
    price: 109.95,
    description: "Fits a laptop",
    category: "men's clothing",
    image: "https://img.test/1.jpg",
    rating: { rate: 3.9, count: 120 },
  },
  {
    id: 2,
    title: "No rating yet",
    price: 5,
    description: "d",
    category: "misc",
    image: "https://img.test/2.jpg",
  },
];

const jsonResponse = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });

describe("FakeStoreProductSource", () => {
  it("maps upstream products and defaults a missing rating", async () => {
    const fetchFn = vi.fn().mockResolvedValue(jsonResponse(upstream));
    const source = new FakeStoreProductSource("https://fake.test/", 60_000, fetchFn);

    const products = await source.listProducts();

    expect(fetchFn).toHaveBeenCalledWith("https://fake.test/products", expect.anything());
    expect(products[0]?.rating).toEqual({ rate: 3.9, count: 120 });
    expect(products[1]?.rating).toEqual({ rate: 0, count: 0 });
  });

  it("caches within the TTL and refetches after it", async () => {
    let now = 0;
    const fetchFn = vi.fn().mockImplementation(async () => jsonResponse(upstream));
    const source = new FakeStoreProductSource("https://fake.test", 1000, fetchFn, () => now);

    await source.listProducts();
    now = 500;
    await source.listProducts();
    expect(fetchFn).toHaveBeenCalledTimes(1);

    now = 1500;
    await source.listProducts();
    expect(fetchFn).toHaveBeenCalledTimes(2);
  });

  it("serves stale data if a refresh fails", async () => {
    let now = 0;
    const fetchFn = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(upstream))
      .mockRejectedValueOnce(new Error("network down"));
    vi.spyOn(console, "warn").mockImplementation(() => {});
    const source = new FakeStoreProductSource("https://fake.test", 1000, fetchFn, () => now);

    await source.listProducts();
    now = 5000;
    expect(await source.listProducts()).toHaveLength(2);
  });

  it("throws ProductSourceError on upstream HTTP errors", async () => {
    const fetchFn = vi.fn().mockResolvedValue(jsonResponse({}, 500));
    const source = new FakeStoreProductSource("https://fake.test", 1000, fetchFn);
    await expect(source.listProducts()).rejects.toBeInstanceOf(ProductSourceError);
  });

  it("throws ProductSourceError on an unexpected payload", async () => {
    const fetchFn = vi.fn().mockResolvedValue(jsonResponse([{ id: "nope" }]));
    const source = new FakeStoreProductSource("https://fake.test", 1000, fetchFn);
    await expect(source.listProducts()).rejects.toBeInstanceOf(ProductSourceError);
  });

  it("getProduct returns null for a missing id", async () => {
    const fetchFn = vi.fn().mockResolvedValue(jsonResponse(upstream));
    const source = new FakeStoreProductSource("https://fake.test", 1000, fetchFn);
    expect(await source.getProduct(99)).toBeNull();
    expect((await source.getProduct(1))?.title).toBe("Backpack");
  });
});
