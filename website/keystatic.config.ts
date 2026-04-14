import { config, fields, collection } from '@keystatic/core';
import React from 'react';

export default config({
  ui: {
    brand: {
      name: 'MARSLib CMS',
      mark: () => React.createElement('img', { src: '/mars-logo.png', height: 28, alt: 'MARSLib Logo' })
    }
  },
  storage: {
    kind: 'github',
    repo: 'MARSProgramming/MARSLib'
  },
  collections: {
    tutorials: collection({
      label: 'Tutorials',
      slugField: 'title',
      path: 'website/src/content/docs/tutorials/**',
      format: { contentField: 'content' },
      schema: {
        title: fields.slug({ name: { label: 'Title' } }),
        sidebar: fields.object({
          order: fields.integer({ label: 'Sidebar Order', isRequired: false }),
        }, { label: 'Sidebar Settings' }),
        content: fields.mdx({
          label: 'Content',
        }),
      },
    }),
  },
});
