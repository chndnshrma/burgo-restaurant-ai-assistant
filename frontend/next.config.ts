import type { NextConfig } from "next";

const API_URL = process.env.API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  // The browser only ever talks to this site. Next forwards /api/* to the Spring Boot backend,
  // so there is no CORS to configure and the backend URL never reaches the client.
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${API_URL}/api/:path*` }];
  },
};

export default nextConfig;
