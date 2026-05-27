/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        mint: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          850: '#14532d',
        },
        health: {
          primary: '#10b981', // Emerald
          dark: '#064e3b', // Deep green
          light: '#ecfdf5', // Soft pastels
          accent: '#f59e0b', // Amber (calories warning)
          coral: '#f43f5e', // Rose
          slate: '#0f172a', // Slate dark
        }
      },
    },
  },
  plugins: [],
}
