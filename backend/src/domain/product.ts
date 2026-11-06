/**
 * The catalogue's own product model. Everything above the ProductSource
 * boundary (service, routes, Android app) depends on this shape only,
 * never on the shape of an upstream API.
 */
export interface Product {
  id: number;
  title: string;
  price: number;
  description: string;
  category: string;
  image: string;
  rating: {
    rate: number;
    count: number;
  };
}
