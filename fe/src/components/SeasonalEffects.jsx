import React from 'react';
import { useSelector } from 'react-redux';
import ChristmasDecorations from './theme/christmas/ChristmasDecorations';
import SummerDecorations from './theme/summer/SummerDecorations';

export default function SeasonalEffects() {
  const { currentTheme } = useSelector((state) => state.theme);

  if (currentTheme === 'christmas') return <ChristmasDecorations />;
  if (currentTheme === 'summer') return <SummerDecorations />;
  return null;
}