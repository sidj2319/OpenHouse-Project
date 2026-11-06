import "dotenv/config";
import { createApp } from "./app";
import { loadConfig } from "./config";
import { LogEmailSender } from "./email/logEmailSender";
import { SmtpEmailSender } from "./email/smtpEmailSender";
import { FakeStoreProductSource } from "./providers/fakeStoreProductSource";
import { CatalogueService } from "./services/catalogueService";
import { EnquiryService } from "./services/enquiryService";

// Composition root: the only place that knows which concrete implementations are used.
const config = loadConfig();

const productSource = new FakeStoreProductSource(config.fakeStoreBaseUrl, config.productCacheTtlMs);
const emailSender = config.smtp ? new SmtpEmailSender(config.smtp) : new LogEmailSender();

const catalogue = new CatalogueService(productSource, {
  featuredIds: config.featuredProductIds,
  featuredCount: 5,
  categoryTopCount: 3,
});
const enquiries = new EnquiryService(catalogue, emailSender, config.salesInboxEmail, config.emailFrom);

const app = createApp({ catalogue, enquiries });
app.listen(config.port, () => {
  console.log(`Catalogue API listening on :${config.port}`);
});
