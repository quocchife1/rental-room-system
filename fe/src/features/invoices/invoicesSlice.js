import { createSlice } from '@reduxjs/toolkit';

const invoicesSlice = createSlice({
  name: 'invoices',
  initialState: { items: [], loading: false },
  reducers: {}
});

export default invoicesSlice.reducer;