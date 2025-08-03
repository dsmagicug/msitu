#!/usr/bin/env node

/**
 * Script to generate config.json for app updates
 * Usage: node scripts/generate-config.js <version> <description>
 * Example: node scripts/generate-config.js 1.1.0 "Bug fixes and performance improvements"
 */

const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);

if (args.length < 2) {
  console.log('Usage: node scripts/generate-config.js <version> <description>');
  console.log('Example: node scripts/generate-config.js 1.1.0 "Bug fixes and performance improvements"');
  process.exit(1);
}

const version = args[0];
const description = args[1];
const changelog = args[2] ? args[2].split(',').map(item => item.trim()) : [];

const config = {
  currentVersion: version,
  previousVersion: "1.0.0", // You can update this manually or pass as parameter
  size: "~61MB",
  downloadUrl: `https://github.com/ekeeya/files/raw/main/msitu-apk-${version}.apk`,
  publishedAt: new Date().toISOString(),
  changelog: changelog.length > 0 ? changelog : [
    "Bug fixes and performance improvements",
    "Enhanced app stability",
    "Improved user experience"
  ]
};

// Write config to file
const configPath = path.join(__dirname, '..', 'config.json');
fs.writeFileSync(configPath, JSON.stringify(config, null, 2));

console.log(`✅ Generated config.json for version ${version}`);
console.log(`📁 Config saved to: ${configPath}`);
console.log(`🔗 APK URL: ${config.downloadUrl}`);
console.log('\n📋 Next steps:');
console.log('1. Upload the config.json to https://github.com/ekeeya/files/raw/main/');
console.log(`2. Upload your APK as msitu-apk-${version}.apk`);
console.log('3. Update the checksums in config.json if needed');
console.log('4. Test the update flow in your app');

// Also generate a sample config for reference
const sampleConfigPath = path.join(__dirname, '..', 'config.json.example');
if (!fs.existsSync(sampleConfigPath)) {
  fs.writeFileSync(sampleConfigPath, JSON.stringify(config, null, 2));
  console.log(`📄 Sample config saved to: ${sampleConfigPath}`);
} 