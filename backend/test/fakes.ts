import type { Product } from "../src/domain/product";
import type { EmailMessage, EmailSender } from "../src/email/emailSender";
import { ProductSourceError, type ProductSource } from "../src/providers/productSource";

const p = (
  id: number,
  title: string,
  category: string,
  rate: number,
  count: number,
  description = `Description of ${title}`,
): Product => ({
  id,
  title,
  price: 10 + id,
  description,
  category,
  image: `https://img.test/${id}.png`,
  rating: { rate, count },
});

export const PRODUCTS: Product[] = [
  p(1, "Blue Backpack", "bags", 3.9, 120),
  p(2, "Red Backpack", "bags", 4.7, 80),
  p(3, "Leather Satchel", "bags", 4.1, 50),
  p(4, "Canvas Tote", "bags", 2.5, 10),
  p(5, "Gold Ring", "jewelery", 4.8, 300),
  p(6, "Silver Chain", "jewelery", 4.2, 90),
  p(7, "Slim Fit T-Shirt", "men's clothing", 4.3, 200),
  p(8, "Rain Jacket", "men's clothing", 4.0, 70, "Waterproof backpack-friendly jacket"),
];

export class FakeProductSource implements ProductSource {
  calls = 0;
  failing = false;

  constructor(private readonly products: Product[] = PRODUCTS) {}

  async listProducts(): Promise<Product[]> {
    this.calls++;
    if (this.failing) throw new ProductSourceError("boom");
    return this.products;
  }

  async getProduct(id: number): Promise<Product | null> {
    return (await this.listProducts()).find((x) => x.id === id) ?? null;
  }
}

export class FakeEmailSender implements EmailSender {
  sent: EmailMessage[] = [];
  error: Error | null = null;

  async send(message: EmailMessage): Promise<void> {
    if (this.error) throw this.error;
    this.sent.push(message);
  }
}
