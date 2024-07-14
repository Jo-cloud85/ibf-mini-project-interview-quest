/** @type {import('tailwindcss').Config} */
import { fontFamily } from "tailwindcss/defaultTheme";
import plugin from "tailwindcss/plugin";

// module.exports = {
//   content: [
//     "./src/**/*.{html,ts}",
//   ],
//   theme: {
//     extend: {},
//   },
//   plugins: [],
// }

module.exports = {
  content: [
    "./src/**/*.{html,ts}",
    "./node_modules/flowbite/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        color: {
          1: "#15131D",
          2: "#FFC876",
        },
        stroke: {
          1: "#FE7E35",
          2: "#6C7275"
        },
        n: {
          0: "#F7F6F4",
          1: "#0E0C15",
          2: "#FE7E35",
          3: "#EDEAE7"
        },
      },
      fontFamily: {
        sans: ["var(--font-sora)", ...fontFamily.sans],
        code: "var(--font-code)",
        grotesk: "var(--font-grotesk)",
      },
      letterSpacing: {
        tagline: ".15em",
      },
      spacing: {
        0.25: "0.0625rem",
        7.5: "1.875rem",
        15: "3.75rem",
      },
      opacity: {
        15: ".15",
      },
      transitionDuration: {
        DEFAULT: "200ms",
      },
      transitionTimingFunction: {
        DEFAULT: "linear",
      },
      zIndex: {
        1: "1",
        2: "2",
        3: "3",
        4: "4",
        5: "5",
      },
      borderWidth: {
        DEFAULT: "0.0625rem",
      },
    },
  },
  plugins: [
    require('flowbite/plugin'),
    plugin(function ({ addBase, addComponents, addUtilities }) {
      addBase({});
      addComponents({
        ".container": {
          "@apply max-w-[77.5rem] mx-auto px-5 md:px-10 lg:px-15 xl:max-w-[87.5rem]":
            {},
        },
        ".h1": {
          "@apply font-semibold text-[2.5rem] leading-[3.25rem] md:text-[2.75rem] md:leading-[3.75rem] lg:text-[3.25rem] lg:leading-[4.0625rem] xl:text-[3.75rem] xl:leading-[4.5rem]":
            {},
        },
        ".h2": {
          "@apply text-[1.75rem] leading-[2.5rem] md:text-[2rem] md:leading-[2.5rem] lg:text-[2.5rem] lg:leading-[3.5rem] xl:text-[3rem] xl:leading-tight":
            {},
        },
        ".h3": {
          "@apply text-[2rem] leading-normal md:text-[2.5rem]": {},
        },
        ".h4": {
          "@apply text-[2rem] leading-normal": {},
        },
        ".h5": {
          "@apply text-2xl leading-normal": {},
        },
        ".h6": {
          "@apply font-semibold text-lg leading-8": {},
        },
        ".body-1": {
          "@apply text-[0.875rem] leading-[1.5rem] md:text-[1rem] md:leading-[1.75rem] lg:text-[1.25rem] lg:leading-8": {},
        },
        ".body-2": {
          "@apply font-light text-[0.875rem] leading-6 md:text-base": {},
        },
        ".custom-button-01": {
          "@apply rounded-full border-2 border-n-2 bg-transparent text-n-2 px-6 py-2 hover:bg-orange-400 hover:text-white transition duration-300": {},
        },
        ".orange-button-01": {
          "@apply inline-flex justify-center py-3 px-4 border border-transparent shadow-sm text-sm font-medium rounded-lg text-white bg-n-2 hover:bg-n-1 transition duration-300": {},
        },
        ".orange-button-02": {
          "@apply absolute top-0 end-0 p-4 inline-flex justify-center rounded-e-lg text-white bg-n-2 hover:bg-n-1 transition duration-300": {},
        },
        ".orange-outline-button-01": {
          "@apply inline-flex justify-center py-3 px-4 border border-orange-500 text-n-2 shadow-sm text-sm font-medium rounded-lg bg-transparent hover:bg-n-1 hover:text-n-0 hover:border-transparent transition duration-300": {},
        },
        ".orange-outline-button-02": {
          "@apply inline-flex justify-center p-3 border-2 border-orange-500 text-n-2 shadow-sm text-sm font-medium rounded-full bg-transparent hover:bg-n-1 hover:text-n-0 hover:border-transparent transition duration-300": {},
        },
        ".auth-form": {
          "@apply w-[32rem] mx-auto dark:bg-zinc-900 shadow-2xl rounded-2xl overflow-hidden bg-stroke-1 border-gray-300 dark:border-gray-200": {},
        },
        ".form-label": {
          "@apply block text-n-1 font-semibold mb-2": {}
        },
        ".form-input": {
          "@apply w-full p-3 border border-stroke-1 rounded-lg bg-transparent placeholder-n-1 placeholder-opacity-25 font-thin focus:outline-none focus:ring-2 focus:ring-stroke-1 focus:border-transparent": {}
        },
        ".form-input-02": {
          "@apply w-96 p-3 border border-none rounded-lg bg-n-3 placeholder-n-1 placeholder-opacity-25 font-thin focus:outline-none focus:ring-2 focus:ring-stroke-1 focus:border-transparent": {}
        },
        ".nav-tab": {
          "@apply flex gap-3 px-6 py-4 justify-items-center rounded-lg hover:bg-n-2 transition duration-200 cursor-pointer": {}
        },
        ".box-background": {
          "@apply cursor-pointer flex items-center justify-center gap-x-4 w-full p-5 font-semibold text-base text-left text-n-1 bg-transparent border border-n-3  focus:ring-4 focus:ring-gray-200 dark:focus:ring-gray-800 dark:border-gray-700 dark:text-gray-400 hover:bg-n-3 dark:hover:bg-gray-800": {}
        },
      });
      addUtilities({
        ".tap-highlight-color": {
          "-webkit-tap-highlight-color": "rgba(0, 0, 0, 0)",
        },
      });
    })
  ],
};