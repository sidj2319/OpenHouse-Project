import type { EmailSender } from "../email/emailSender";
import type { CatalogueService } from "./catalogueService";

export interface EnquiryInput {
  productId: number;
  name: string;
  email: string;
  phone?: string | undefined;
  message?: string | undefined;
}

export class ProductNotFoundError extends Error {
  constructor(productId: number) {
    super(`Product ${productId} not found`);
    this.name = "ProductNotFoundError";
  }
}

const oneLine = (s: string): string => s.replace(/[\r\n]+/g, " ").trim();

export class EnquiryService {
  constructor(
    private readonly catalogue: CatalogueService,
    private readonly sender: EmailSender,
    private readonly salesInbox: string,
    private readonly from: string,
  ) {}

  /** Resolves only once the email has been handed to the provider. */
  async submit(input: EnquiryInput): Promise<void> {
    const product = await this.catalogue.get(input.productId);
    if (!product) throw new ProductNotFoundError(input.productId);

    const lines = [
      `New product enquiry`,
      ``,
      `Product: ${oneLine(product.title)} (#${product.id})`,
      `Category: ${product.category}`,
      `Price: $${product.price.toFixed(2)}`,
      ``,
      `Customer: ${oneLine(input.name)}`,
      `Email: ${input.email}`,
      `Phone: ${input.phone ? oneLine(input.phone) : "not provided"}`,
      ``,
      `Message:`,
      input.message?.trim() || "(none)",
    ];

    await this.sender.send({
      from: this.from,
      to: this.salesInbox,
      replyTo: input.email,
      subject: `New enquiry: ${oneLine(product.title)} (#${product.id})`,
      text: lines.join("\n"),
    });
  }
}
