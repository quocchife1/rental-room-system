import React, { createContext, useContext } from 'react';

const ThemeRequestContext = createContext(null);

export function ThemeRequestProvider({ requestTheme, children }) {
  return (
    <ThemeRequestContext.Provider value={requestTheme ?? null}>
      {children}
    </ThemeRequestContext.Provider>
  );
}

export function useThemeRequest() {
  return useContext(ThemeRequestContext);
}
