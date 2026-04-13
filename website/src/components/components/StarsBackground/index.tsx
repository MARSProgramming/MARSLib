import React from 'react';
import styles from './styles.module.css';

export default function StarsBackground(): JSX.Element {
  return (
    <div className={styles.starsContainer}>
      <div className={styles.stars}></div>
    </div>
  );
}
