import axios from 'axios';

const API_URL = '/api';

export const getCustomers = () => axios.get(`${API_URL}/customers/getAllCustomers`);
export const getCustomerByPhone = (phone) => axios.get(`${API_URL}/customers/getByPhone/${phone}`);
export const createCustomer = (customer) => axios.post(`${API_URL}/customers`, customer);
export const getCustomerVehicles = (phone) => axios.get(`${API_URL}/customers/getVehicles/${phone}`);

export const getBrands = () => axios.get(`${API_URL}/brands`);

export const createVehicle = (vehicle) => axios.post(`${API_URL}/vehicles`, vehicle);

export const createAppointment = (plate) => axios.post(`${API_URL}/appointments`, null, { params: { plate } });
export const getAppointments = () => axios.get(`${API_URL}/appointments`);
export const updateAppointmentStatus = (id, status) => axios.put(`${API_URL}/appointments/${id}`, null, { params: { status } });

export const getStations = () => axios.get(`${API_URL}/stations`);
export const createStation = (station) => axios.post(`${API_URL}/stations`, station);
export const updateStation = (station) => axios.put(`${API_URL}/stations`, station);

export const createInspection = (inspection) => axios.post(`${API_URL}/inspections`, inspection);
export const updateInspection = (id, inspection) => axios.put(`${API_URL}/inspections/${id}`, inspection);
export const completeInspection = (id, details) => axios.post(`${API_URL}/inspections/${id}/complete`, details);
export const getInspections = () => axios.get(`${API_URL}/inspections`);

export const getChecklistTemplates = () => axios.get(`${API_URL}/checklist-templates`);
