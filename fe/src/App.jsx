import React from 'react';
import { BrowserRouter, useRoutes } from 'react-router-dom';
import appRoutes from './routes';

// Component này sẽ chịu trách nhiệm render routes từ mảng config
const AppRoutes = () => {
  const element = useRoutes(appRoutes);
  return element;
};

function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;