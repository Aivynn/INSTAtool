import React from 'react';
import { Route, Routes } from 'react-router-dom';
import Main from './main';

const AppRoutes = () => (
  <Routes>
    <Route index element={<Main />} />
  </Routes>
);

export default AppRoutes;
