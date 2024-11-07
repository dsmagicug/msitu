/** @type {import('tailwindcss').Config} */
module.exports = {
  // NOTE: Update this to include the paths to all of your component files.
  content: [
    './index.{js,ts,jsx,tsx}',
    './App.{js,ts,jsx,tsx}',
    './src/screens/**/*.{js,ts,jsx,tsx}',
    './src/components/**/*.{js,ts,jsx,tsx}',
    './src/components/*.{js,ts,jsx,tsx}',
    './src/navigation/*.{js,ts,jsx,tsx}',
  ],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      fontFamily: { 
        avenir: ['AvenirBook', 'sans-serif'],
        avenirMedium: ['AvenirMedium', 'sans-serif'],
        avenirBold: ['AvenirBold', 'sans-serif']
      },
    },
  },
  plugins: [],
}
