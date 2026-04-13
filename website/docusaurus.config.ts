import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'MARSLib',
  tagline: 'Championship-tier FRC software framework with zero-allocation performance, dyn4j physics simulation, and AdvantageKit deterministic logging.',
  favicon: 'img/favicon.ico',

  url: 'https://MARSProgramming.github.io',
  baseUrl: '/MARSLib/',

  organizationName: 'MARSProgramming',
  projectName: 'MARSLib',

  onBrokenLinks: 'warn',

  markdown: {
    format: 'mdx',
    mermaid: true,
    mdx1Compat: {
      comments: true,
      admonitions: true,
      headingIds: true,
    },
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          editUrl: 'https://github.com/MARSProgramming/MARSLib/tree/master/website/',
        },
        blog: {
          showReadingTime: true,
          editUrl: 'https://github.com/MARSProgramming/MARSLib/tree/master/website/',
        },
        theme: {
          customCss: ['./src/css/custom.css', './src/css/legacy.css'],
        },
      } satisfies Preset.Options,
    ],
  ],

  scripts: [
    '/MARSLib/assets/js/common.js', // Interactive UI features from legacy
    {
      src: 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js',
      async: true,
    },
  ],

  clientModules: [
    './src/mermaidInit.ts',
  ],

  plugins: [
    /*
    [
      require.resolve("docusaurus-plugin-search-local"),
      {
        hashed: true,
        indexDocs: true,
        indexBlog: true,
      },
    ],
    */
  ],

  themes: [
    [
      require.resolve("@easyops-cn/docusaurus-search-local"),
      {
        hashed: true,
        indexDocs: false,
        indexPages: true,
      },
    ],
  ],

  themeConfig: {
    colorMode: {
      defaultMode: 'dark',
      disableSwitch: true,
      respectPrefersColorScheme: false,
    },
    navbar: {
      title: 'MARSLib',
      logo: {
        alt: 'MARS Logo',
        src: 'assets/mars-logo.png',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Tutorials',
        },
        {to: '/standards', label: 'Core Standards', position: 'left'},
        {to: '/javadoc/index.html', label: 'API Reference', position: 'left'},
        {
          href: 'https://github.com/MARSProgramming/MARSLib',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Getting Started',
              to: '/docs/tutorials/getting-started',
            },
            {
              label: 'All Tutorials',
              to: '/docs/intro',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'MARS Website',
              href: 'https://www.marsfirst.org',
            },
            {
              label: 'Hall of Fame',
              href: 'https://www.firsthalloffame.org',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'Core Standards',
              to: '/standards',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/MARSProgramming/MARSLib',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Mountaineer Area RoboticS — FRC Team 2614. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'groovy'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
