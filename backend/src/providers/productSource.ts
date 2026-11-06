import type { Product } from "../domain/product";

/**
 * Boundary between the catalogue and wherever products actually live.
 * Swap the Fake Store API for a database, CMS or ERP by implementing this
 * interface and changing one line in server.ts.
 */
export interface ProductSource {
  listProducts(): Promise<Product[]>;
  getProduct(id: number): Promise<Product | null>;
}

/** Thrown when the underlying product data cannot be obtained. */
export class ProductSourceError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = "ProductSourceError";
  }
}
