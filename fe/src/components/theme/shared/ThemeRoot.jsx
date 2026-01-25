import React, { useCallback, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setTheme, setTransitioning } from '../../../features/theme/themeSlice';
import ThemeTransitionOverlay from './ThemeTransitionOverlay';
import ChristmasDecorations from '../christmas/ChristmasDecorations';
import SummerDecorations from '../summer/SummerDecorations';
import { ThemeRequestProvider } from './ThemeRequestContext';

export default function ThemeRoot({ children }) {
  const dispatch = useDispatch();
  const { currentTheme, isTransitioning } = useSelector((state) => state.theme);
  const [pendingTheme, setPendingTheme] = useState(null);

  const requestTheme = useCallback(
    (nextTheme) => {
      if (!nextTheme) return;
      if (isTransitioning) return;
      if (nextTheme === currentTheme) return;

      setPendingTheme(nextTheme);
      dispatch(setTransitioning(true));
    },
    [dispatch, isTransitioning, currentTheme]
  );

  const onMidpoint = useCallback(() => {
    if (!pendingTheme) return;
    dispatch(setTheme(pendingTheme));
  }, [dispatch, pendingTheme]);

  const onDone = useCallback(() => {
    dispatch(setTransitioning(false));
    setPendingTheme(null);
  }, [dispatch]);

  const decorations = useMemo(() => {
    if (currentTheme === 'christmas') return <ChristmasDecorations />;
    if (currentTheme === 'summer') return <SummerDecorations />;
    return null;
  }, [currentTheme]);

  return (
    <ThemeRequestProvider requestTheme={requestTheme}>
      <div className="min-h-screen" data-theme={currentTheme}>
        <div className={isTransitioning ? 'theme-transitioning' : undefined}>
          {decorations}
          {children}
        </div>
        <ThemeTransitionOverlay
          active={isTransitioning}
          onMidpoint={onMidpoint}
          onDone={onDone}
        />
      </div>
    </ThemeRequestProvider>
  );
}
