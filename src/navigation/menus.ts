// Navigation menus configuration
export const DRAWER_MENUS = {
  projects: {
    title: "Projects",
    items: [
      {
        label: "Project List",
        description: "View and manage projects",
        icon: "folder-outline",
        iconType: "Ionicon",
        action: "setShowProjectList",
        comingSoon: false,
      },
      {
        label: "Import Projects",
        description: "Import from cloud or local files",
        icon: "download",
        iconType: "Entypo",
        action: "console.log",
        comingSoon: true,
      },
      {
        label: "Export Projects",
        description: "Share and backup your data",
        icon: "code-json",
        iconType: "MaterialCommunityIcons",
        action: "console.log",
        comingSoon: true,
      },
    ]
  },
  equipment: {
    title: "GNSS Equipment",
    items: [
      {
        label: "Bluetooth Devices",
        description: "Connect to GNSS receivers",
        icon: "bluetooth",
        iconType: "Ionicon",
        action: "setShowBTDevices",
        comingSoon: false,
      },
      {
        label: "USB Serial",
        description: "Direct USB connections",
        icon: "usb-port",
        iconType: "MaterialCommunityIcons",
        action: "console.log",
        comingSoon: true,
      },
    ]
  },
  application: {
    title: "Application",
    items: [
      {
        label: "Settings",
        description: "Configure app preferences",
        icon: "settings-outline",
        iconType: "Ionicon",
        action: "navigate",
        actionParams: "Settings",
        comingSoon: false,
      },
      {
        label: "About Msitu",
        description: "Version and information",
        icon: "information-circle-outline",
        iconType: "Ionicon",
        action: "setShowAboutMsitu",
        comingSoon: false,
      },
    ]
  }
}; 