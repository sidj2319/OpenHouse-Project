import { z } from "zod";
import type { Product } from "../domain/product";
import { ProductSourceError, type ProductSource } from "./productSource";

// Anti-corruption layer: validate the upstream payload and map it to our model.
const upstreamProductSchema = z.object({
  id: z.number().int(),
  title: z.string(),
  price: z.number(),
  description: z.string(),
  category: z.string(),
  image: z.string(),
  rating: z.object({ rate: z.number(), count: z.number() }).optional(),
});
const upstreamListSchema = z.array(upstreamProductSchema);

const REQUEST_TIMEOUT_MS = 5000;

export class FakeStoreProductSource implements ProductSource {
  private cache: { fetchedAt: number; products: Product[] } | null = null;

  constructor(
    private readonly baseUrl: string,
    private readonly cacheTtlMs: number,
    private readonly fetchFn: typeof fetch = fetch,
    private readonly now: () => number = Date.now,
  ) {}

  async listProducts(): Promise<Product[]> {
    if (this.cache && this.now() - this.cache.fetchedAt < this.cacheTtlMs) {
      return this.cache.products;
    }
    try {
      const products = await this.fetchAll();
      this.cache = { fetchedAt: this.now(), products };
      return products;
    } catch (err) {
      // Prefer slightly stale data over failing the whole catalogue.
      if (this.cache) {
        console.warn("Product source unavailable, serving stale data:", err);
        return this.cache.products;
      }
      throw err instanceof ProductSourceError
        ? err
        : new ProductSourceError("Failed to load products", { cause: err });
    }
  }

  async getProduct(id: number): Promise<Product | null> {
    const products = await this.listProducts();
    return products.find((p) => p.id === id) ?? null;
  }

  private async fetchAll(): Promise<Product[]> {
    const res = await this.fetchFn(`${this.baseUrl.replace(/\/$/, "")}/products`, {
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
      headers: { Accept: "application/json" },
    });
    if (!res.ok) {
      throw new ProductSourceError(`Upstream responded with HTTP ${res.status}`);
    }
    const parsed = upstreamListSchema.safeParse(await res.json());
    if (!parsed.success) {
      throw new ProductSourceError("Upstream returned an unexpected payload", {
        cause: parsed.error,
      });
    }
    return parsed.data.map((p) => ({
      id: p.id,
      title: p.title,
      price: p.price,
      description: p.description,
      category: p.category,
      image: p.image,
      rating: p.rating ?? { rate: 0, count: 0 },
    }));
  }
}
