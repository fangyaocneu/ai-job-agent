import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      "/api": {
        target:
          "http://ai-job-agent-api-alb-1839578584.us-west-2.elb.amazonaws.com",
        changeOrigin: true,
      },
    },
  },
});