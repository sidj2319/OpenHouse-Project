import { describe, expect, it } from "vitest";
import { CatalogueService } from "../src/services/catalogueService";
import { FakeProductSource } from "./fakes";

const make = (featuredIds: number[] = []) =>
  new CatalogueService(new FakeProductSource(), { featuredIds, featuredCount: 5, categoryTopCount: 2 });

describe("featured", () => {
  it("returns the curated products in the configured order", async () => {
    const featured = await make([8, 7, 6, 5, 4]).featured();
    expect(featured.map((x) => x.id)).toEqual([8, 7, 6, 5, 4]);
  });

  it("ignores unknown and duplicate ids, then fills by highest rating", async () => {
    const featured = await make([999, 1, 1]).featured();
    expect(featured).toHaveLength(5);
    expect(featured[0]?.id).toBe(1);
    // fill order: 5 (4.8), 2 (4.7), 7 (4.3), 6 (4.2)
    expect(featured.slice(1).map((x) => x.id)).toEqual([5, 2, 7, 6]);
  });

  it("falls back to top rated when nothing is curated", async () => {
    const featured = await make().featured();
    expect(featured.map((x) => x.id)).toEqual([5, 2, 7, 6, 3]);
  });

  it("never returns more than the configured count", async () => {
    const featured = await make([1, 2, 3, 4, 5, 6, 7]).featured();
    expect(featured.map((x) => x.id)).toEqual([1, 2, 3, 4, 5]);
  });
});

describe("categories", () => {
  it("lists distinct categories alphabetically", async () => {
    expect(await make().categories()).toEqual(["bags", "jewelery", "men's clothing"]);
  });
});

describe("category", () => {
  it("splits into top rated and additional products", async () => {
    const listing = await make().category("bags");
    expect(listing?.topRated.map((x) => x.id)).toEqual([2, 3]);
    expect(listing?.others.map((x) => x.id)).toEqual([1, 4]);
  });

  it("matches case-insensitively and returns the canonical name", async () => {
    const listing = await make().category("Men's Clothing");
    expect(listing?.category).toBe("men's clothing");
  });

  it("returns null for an unknown category", async () => {
    expect(await make().category("shoes")).toBeNull();
  });

  it("puts everything in topRated when the category is small", async () => {
    const listing = await make().category("jewelery");
    expect(listing?.topRated).toHaveLength(2);
    expect(listing?.others).toEqual([]);
  });
});

describe("search", () => {
  it("matches title text case-insensitively", async () => {
    const results = await make().search("BACKPACK");
    expect(results.map((x) => x.id).sort()).toEqual([1, 2, 8]);
  });

  it("matches description text", async () => {
    const results = await make().search("waterproof");
    expect(results.map((x) => x.id)).toEqual([8]);
  });

  it("requires every term to match", async () => {
    const results = await make().search("red backpack");
    expect(results.map((x) => x.id)).toEqual([2]);
  });

  it("returns nothing when no product matches", async () => {
    expect(await make().search("zeppelin")).toEqual([]);
  });

  it("returns nothing for a blank query", async () => {
    expect(await make().search("   ")).toEqual([]);
  });
});
