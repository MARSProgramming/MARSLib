import { config, fields, collection } from '@keystatic/core';

export default config({
  storage: {
    kind: 'github',
    repo: 'MARSProgramming/MARSLib'
  },
  collections: {
    tutorials: collection({
      label: 'Tutorials',
      slugField: 'title',
      path: 'src/content/docs/tutorials/**/*',
      format: { contentField: 'content' },
      schema: {
        title: fields.slug({ name: { label: 'Title' } }),
        sidebar: fields.object({
          order: fields.integer({ label: 'Sidebar Order', isRequired: false }),
        }, { label: 'Sidebar Settings' }),
        content: fields.markdoc({
          label: 'Content',
        }),
      },
    }),
  },
});
