// App version configuration
export const APP_VERSION = '1.0.0';
export const APP_NAME = 'Msitu';
export const APP_SUBTITLE = 'Every tree planter\'s companion';

// Contact information
export const CONTACT_INFO = {
  phone: '+256765810344',
  email: 'support@ds.co.ug',
  website: 'https://msitu.tech',
  github: 'https://github.com/dsmagicug/msitu',
};

// Version details
export const VERSION_INFO = {
  version: APP_VERSION,
  name: APP_NAME,
  subtitle: APP_SUBTITLE,
  buildNumber: '01',
  buildDate: '2025-08-01',
};

// Helper function to get formatted build info
export const getBuildInfo = () => {
  return `${VERSION_INFO.buildDate}.${VERSION_INFO.buildNumber}`;
};

export default VERSION_INFO;
