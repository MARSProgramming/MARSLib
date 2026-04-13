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

  onBrokenLinks: 'throw',

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
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
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

  themeConfig: {
    colorMode: {
      defaultMode: 'dark',
      disableSwitch: false,
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'MARSLib',
      logo: {
        alt: 'MARS Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Tutorials',
        },
        {to: '/blog', label: 'Blog', position: 'left'},
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
              label: 'Blog',
              to: '/blog',
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
