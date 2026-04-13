import React from 'react';
import StarsBackground from '@site/src/components/StarsBackground';

export default function Root({children}: {children: React.ReactNode}): JSX.Element {
  return (
    <>
      <StarsBackground />
      {children}
    </>
  );
}
