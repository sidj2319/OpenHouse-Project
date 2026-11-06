import type { Product } from "../domain/product";
import type { ProductSource } from "../providers/productSource";

export interface CatalogueOptions {
  /** Curated "top rated" product IDs for the home page, in display order. */
  featuredIds: number[];
  /** How many products the home page features. */
  featuredCount: number;
  /** How many "top rated" products a category page leads with. */
  categoryTopCount: number;
}

export interface CategoryListing {
  category: string;
  topRated: Product[];
  others: Product[];
}

const byRating = (a: Product, b: Product): number =>
  b.rating.rate - a.rating.rate || b.rating.count - a.rating.count || a.id - b.id;

export class CatalogueService {
  constructor(
    private readonly source: ProductSource,
    private readonly options: CatalogueOptions,
  ) {}

  /** Curated products first; any shortfall is filled by highest rating. */
  async featured(): Promise<Product[]> {
    const all = await this.source.listProducts();
    const byId = new Map(all.map((p) => [p.id, p]));
    const curated: Product[] = [];
    for (const id of new Set(this.options.featuredIds)) {
      const product = byId.get(id);
      if (product) curated.push(product);
    }
    const picked = curated.slice(0, this.options.featuredCount);
    if (picked.length >= this.options.featuredCount) return picked;

    const taken = new Set(picked.map((p) => p.id));
    const fill = all
      .filter((p) => !taken.has(p.id))
      .sort(byRating)
      .slice(0, this.options.featuredCount - picked.length);
    return [...picked, ...fill];
  }

  async categories(): Promise<string[]> {
    const all = await this.source.listProducts();
    return [...new Set(all.map((p) => p.category))].sort();
  }

  /** Returns null when the category does not exist. Matching is case-insensitive. */
  async category(name: string): Promise<CategoryListing | null> {
    const all = await this.source.listProducts();
    const wanted = name.trim().toLowerCase();
    const items = all.filter((p) => p.category.toLowerCase() === wanted);
    const first = items[0];
    if (!first) return null;

    const sorted = [...items].sort(byRating);
    const top = this.options.categoryTopCount;
    return { category: first.category, topRated: sorted.slice(0, top), others: sorted.slice(top) };
  }

  /**
   * Every whitespace-separated term must appear (case-insensitively) in the
   * title, description or category.
   */
  async search(query: string): Promise<Product[]> {
    const terms = query.toLowerCase().split(/\s+/).filter(Boolean);
    if (terms.length === 0) return [];
    const all = await this.source.listProducts();
    return all
      .filter((p) => {
        const haystack = `${p.title} ${p.description} ${p.category}`.toLowerCase();
        return terms.every((t) => haystack.includes(t));
      })
      .sort(byRating);
  }

  get(id: number): Promise<Product | null> {
    return this.source.getProduct(id);
  }
}
