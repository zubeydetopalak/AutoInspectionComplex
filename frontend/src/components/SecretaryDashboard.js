import React, { useState, useEffect } from 'react';
import {
    getCustomers,
    getCustomerByPhone,
    createCustomer,
    getCustomerVehicles,
    getBrands,
    createVehicle,
    createAppointment,
    getStations,
    getAppointments,
    updateAppointmentStatus,
    createStation,
    updateStation
} from '../services/api';

const SecretaryDashboard = () => {
    const [activeTab, setActiveTab] = useState('customers'); // 'customers' or 'stations'

    // Customer State
    const [allCustomers, setAllCustomers] = useState([]);
    const [phoneSearch, setPhoneSearch] = useState('');
    const [currentCustomer, setCurrentCustomer] = useState(null);
    const [customerNotFound, setCustomerNotFound] = useState(false);
    const [newCustomer, setNewCustomer] = useState({ name: '', email: '', phone: '' });

    // Vehicle State
    const [vehicles, setVehicles] = useState([]);
    const [brands, setBrands] = useState([]);
    const [showAddVehicle, setShowAddVehicle] = useState(false);
    const [newVehicle, setNewVehicle] = useState({
        plateCode: '',
        modelYear: '',
        chassisNumber: '',
        vehicleType: 'Binek',
        brand: { id: '' }
    });

    // Station State
    const [stations, setStations] = useState([]);
    const [showAddStation, setShowAddStation] = useState(false);
    const [editingStation, setEditingStation] = useState(null);
    const [newStation, setNewStation] = useState({
        stationCode: '',
        capacity: 5,
        open: true,
        exclusiveBrand: null
    });

    // Appointment State
    const [appointments, setAppointments] = useState([]);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [editStatus, setEditStatus] = useState('');
    const [showAllAppointments, setShowAllAppointments] = useState(false);

    const [message, setMessage] = useState({ text: '', type: '' });

    useEffect(() => {
        if (activeTab === 'customers') {
            loadCustomers();
        }
        if (activeTab === 'stations') {
            loadStations();
        }
        if ((showAddVehicle || showAddStation || editingStation) && brands.length === 0) {
            loadBrands();
        }
        // Always load appointments to check status
        loadAppointments();
    }, [activeTab, showAddVehicle, showAddStation, editingStation]);

    const loadStations = async () => {
        try {
            const response = await getStations();
            setStations(response.data);
        } catch (error) {
            console.error("Error loading stations", error);
        }
    };

    const loadCustomers = async () => {
        try {
            const response = await getCustomers();
            setAllCustomers(response.data);
        } catch (error) {
            console.error("Error loading customers", error);
        }
    };

    const loadBrands = async () => {
        try {
            const response = await getBrands();
            setBrands(response.data);
        } catch (error) {
            console.error("Error loading brands", error);
        }
    };

    const loadAppointments = async () => {
        try {
            const response = await getAppointments();
            setAppointments(response.data);
        } catch (error) {
            console.error("Error loading appointments", error);
        }
    };

    const handleSearchCustomer = async (e) => {
        e.preventDefault();
        setMessage({ text: '', type: '' });
        setCustomerNotFound(false);
        setCurrentCustomer(null);
        setVehicles([]);
        setShowAllAppointments(false);

        try {
            const response = await getCustomerByPhone(phoneSearch);
            const customer = response.data;
            if (customer) {
                setCurrentCustomer(customer);
                loadCustomerVehicles(customer.phone);
            } else {
                setCustomerNotFound(true);
                setNewCustomer({ ...newCustomer, phone: phoneSearch });
            }
        } catch (error) {
            console.error("Error searching customer", error);
            setCustomerNotFound(true);
            setNewCustomer({ ...newCustomer, phone: phoneSearch });
        }
    };

    const handleShowAllAppointments = () => {
        setShowAllAppointments(true);
        setCurrentCustomer(null);
        setCustomerNotFound(false);
        setVehicles([]);
        loadAppointments();
    };

    const loadCustomerVehicles = async (phone) => {
        try {
            const response = await getCustomerVehicles(phone);
            setVehicles(response.data);
        } catch (error) {
            console.error("Error loading vehicles", error);
        }
    };

    const handleCreateCustomer = async (e) => {
        e.preventDefault();
        try {
            const response = await createCustomer(newCustomer);
            const created = response.data;
            setCurrentCustomer(created);
            setCustomerNotFound(false);
            setMessage({ text: 'Customer registered successfully!', type: 'success' });
        } catch (error) {
            console.error("Error creating customer", error);
            setMessage({ text: 'Failed to register customer.', type: 'error' });
        }
    };

    const handleCreateVehicle = async (e) => {
        e.preventDefault();
        if (!currentCustomer) return;

        try {
            await createVehicle({
                ...newVehicle,
                customer: { id: currentCustomer.id }
            });
            setMessage({ text: 'Vehicle added successfully!', type: 'success' });
            setShowAddVehicle(false);
            loadCustomerVehicles(currentCustomer.phone);
            setNewVehicle({
                plateCode: '',
                modelYear: '',
                chassisNumber: '',
                vehicleType: 'Binek',
                brand: { id: '' }
            });
        } catch (error) {
            console.error("Error creating vehicle", error);
            setMessage({ text: 'Failed to add vehicle.', type: 'error' });
        }
    };

    const handleCreateAppointment = async (plate) => {
        try {
            await createAppointment(plate);
            setMessage({ text: `Appointment created for ${plate}!`, type: 'success' });
            loadAppointments(); // Refresh appointments
        } catch (error) {
            console.error("Error creating appointment", error);
            setMessage({ text: 'Failed to create appointment.', type: 'error' });
        }
    };

    const handleShowDetails = (plate) => {
        // Find the latest appointment for this vehicle
        const vehicleAppointments = appointments.filter(a => a.vehicle && a.vehicle.plateCode === plate);
        if (vehicleAppointments.length > 0) {
            // Sort by ID desc to get latest (assuming ID increments)
            const latest = vehicleAppointments.sort((a, b) => b.id - a.id)[0];
            setSelectedAppointment(latest);
            setEditStatus(latest.status);
            setShowModal(true);
        } else {
            setMessage({ text: 'No appointment found for this vehicle.', type: 'info' });
        }
    };

    const handleUpdateStatus = async () => {
        if (!selectedAppointment) return;
        try {
            const response = await updateAppointmentStatus(selectedAppointment.id, editStatus);
            setSelectedAppointment(response.data);
            setMessage({ text: 'Appointment status updated!', type: 'success' });
            loadAppointments();
        } catch (error) {
            console.error("Error updating status", error);
            setMessage({ text: 'Failed to update status.', type: 'error' });
        }
    };

    const closeModal = () => {
        setShowModal(false);
        setSelectedAppointment(null);
    };

    const handleCreateStation = async (e) => {
        e.preventDefault();
        try {
            await createStation(newStation);
            setMessage({ text: 'Station created successfully!', type: 'success' });
            setShowAddStation(false);
            loadStations();
            setNewStation({ stationCode: '', capacity: 5, open: true, exclusiveBrand: null });
        } catch (error) {
            console.error("Error creating station", error);
            setMessage({ text: 'Failed to create station.', type: 'error' });
        }
    };

    const handleUpdateStation = async (e) => {
        e.preventDefault();
        if (!editingStation) return;
        try {
            await updateStation(editingStation);
            setMessage({ text: 'Station updated successfully!', type: 'success' });
            setEditingStation(null);
            loadStations();
        } catch (error) {
            console.error("Error updating station", error);
            setMessage({ text: 'Failed to update station.', type: 'error' });
        }
    };

    return (
        <div className="dashboard-container">
            {/* Sidebar */}
            <div className="sidebar">
                <div className="sidebar-header">
                    <h3>Auto Service</h3>
                    <p>Secretary Portal</p>
                </div>
                <ul className="sidebar-menu">
                    <li
                        className={activeTab === 'customers' ? 'active' : ''}
                        onClick={() => setActiveTab('customers')}
                    >
                        Customers & Appointments
                    </li>
                    <li
                        className={activeTab === 'stations' ? 'active' : ''}
                        onClick={() => setActiveTab('stations')}
                    >
                        Station Status
                    </li>
                </ul>
            </div>

            {/* Main Content */}
            <div className="main-content">
                <header className="top-bar">
                    <h2>{activeTab === 'customers' ? 'Customer Management' : 'Station Overview'}</h2>
                </header>

                <div className="content-area">
                    {message.text && (
                        <div className={`alert ${message.type}`} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span>{message.text}</span>
                            <button
                                onClick={() => setMessage({ text: '', type: '' })}
                                style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: '1.2em', marginLeft: '10px' }}
                            >
                                &times;
                            </button>
                        </div>
                    )}

                    {activeTab === 'customers' && (
                        <div className="customer-section">
                            {/* Search Bar */}
                            <div className="card search-card">
                                <h3>Find Customer</h3>
                                <form onSubmit={handleSearchCustomer} className="search-form">
                                    <div className="form-group">
                                        <input
                                            type="text"
                                            className="form-input"
                                            placeholder="Enter Phone Number"
                                            value={phoneSearch}
                                            onChange={(e) => setPhoneSearch(e.target.value)}
                                            required
                                        />
                                    </div>
                                    <button type="submit" className="btn btn-primary">Search</button>
                                    <button type="button" className="btn btn-secondary" onClick={handleShowAllAppointments} style={{ marginLeft: '10px' }}>Show All Appointments</button>
                                </form>
                            </div>

                            {/* All Appointments List */}
                            {showAllAppointments && (
                                <div className="card">
                                    <h3>All Appointments</h3>
                                    {appointments.length === 0 ? (
                                        <p>No appointments found.</p>
                                    ) : (
                                        <table className="data-table">
                                            <thead>
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Plate</th>
                                                    <th>Status</th>
                                                    <th>Station</th>
                                                    <th>Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {appointments.map(app => (
                                                    <tr key={app.id}>
                                                        <td>{app.id}</td>
                                                        <td>{app.vehicle ? app.vehicle.plateCode : 'N/A'}</td>
                                                        <td><span className={`status-badge ${app.status}`}>{app.status}</span></td>
                                                        <td>{app.station ? app.station.stationCode : 'Pending'}</td>
                                                        <td>
                                                            <button
                                                                className="btn btn-sm btn-info"
                                                                onClick={() => {
                                                                    setSelectedAppointment(app);
                                                                    setEditStatus(app.status);
                                                                    setShowModal(true);
                                                                }}
                                                            >
                                                                Edit Status
                                                            </button>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    )}
                                </div>
                            )}

                            {/* Customer List */}
                            {!currentCustomer && !customerNotFound && !showAllAppointments && (
                                <div className="card">
                                    <h3>All Customers</h3>
                                    {allCustomers.length === 0 ? (
                                        <p>No customers found.</p>
                                    ) : (
                                        <table className="data-table">
                                            <thead>
                                                <tr>
                                                    <th>Name</th>
                                                    <th>Phone</th>
                                                    <th>Email</th>
                                                    <th>Action</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {allCustomers.map(c => (
                                                    <tr key={c.id}>
                                                        <td>{c.name}</td>
                                                        <td>{c.phone}</td>
                                                        <td>{c.email}</td>
                                                        <td>
                                                            <button className="btn btn-sm btn-info" onClick={() => {
                                                                setCurrentCustomer(c);
                                                                loadCustomerVehicles(c.phone);
                                                            }}>Select</button>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    )}
                                </div>
                            )}

                            {/* New Customer Form */}
                            {customerNotFound && (
                                <div className="card">
                                    <h3>Register New Customer</h3>
                                    <form onSubmit={handleCreateCustomer}>
                                        <div className="form-group">
                                            <label className="form-label">Name</label>
                                            <input
                                                className="form-input"
                                                value={newCustomer.name}
                                                onChange={(e) => setNewCustomer({ ...newCustomer, name: e.target.value })}
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label className="form-label">Email</label>
                                            <input
                                                className="form-input"
                                                type="email"
                                                value={newCustomer.email}
                                                onChange={(e) => setNewCustomer({ ...newCustomer, email: e.target.value })}
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label className="form-label">Phone</label>
                                            <input
                                                className="form-input"
                                                value={newCustomer.phone}
                                                onChange={(e) => setNewCustomer({ ...newCustomer, phone: e.target.value })}
                                                required
                                            />
                                        </div>
                                        <button type="submit" className="btn btn-primary">Register Customer</button>
                                    </form>
                                </div>
                            )}

                            {/* Customer Details & Vehicles */}
                            {currentCustomer && (
                                <div className="customer-details">
                                    <div className="card info-card">
                                        <h3>Customer Info</h3>
                                        <p><strong>Name:</strong> {currentCustomer.name}</p>
                                        <p><strong>Phone:</strong> {currentCustomer.phone}</p>
                                        <p><strong>Email:</strong> {currentCustomer.email}</p>
                                    </div>

                                    <div className="card vehicle-card">
                                        <div className="card-header-flex">
                                            <h3>Vehicles</h3>
                                            <button
                                                className="btn btn-sm btn-primary"
                                                onClick={() => setShowAddVehicle(!showAddVehicle)}
                                            >
                                                {showAddVehicle ? 'Cancel' : 'Add Vehicle'}
                                            </button>
                                        </div>

                                        {showAddVehicle && (
                                            <div className="add-vehicle-form">
                                                <h4>New Vehicle</h4>
                                                <form onSubmit={handleCreateVehicle}>
                                                    <div className="form-grid">
                                                        <div className="form-group">
                                                            <label className="form-label">Plate Code</label>
                                                            <input className="form-input" value={newVehicle.plateCode} onChange={e => setNewVehicle({ ...newVehicle, plateCode: e.target.value })} required />
                                                        </div>
                                                        <div className="form-group">
                                                            <label className="form-label">Brand</label>
                                                            <select
                                                                className="form-input"
                                                                value={newVehicle.brand.id}
                                                                onChange={e => {
                                                                    const selectedId = e.target.value;
                                                                    const selectedBrand = brands.find(b => b.id.toString() === selectedId);
                                                                    setNewVehicle({
                                                                        ...newVehicle,
                                                                        brand: {
                                                                            id: selectedId,
                                                                            name: selectedBrand ? selectedBrand.name : ''
                                                                        }
                                                                    });
                                                                }}
                                                                required
                                                            >
                                                                <option value="">Select Brand</option>
                                                                {brands.map(b => (
                                                                    <option key={b.id} value={b.id}>{b.name}</option>
                                                                ))}
                                                            </select>
                                                        </div>
                                                        <div className="form-group">
                                                            <label className="form-label">Model Year</label>
                                                            <input className="form-input" value={newVehicle.modelYear} onChange={e => setNewVehicle({ ...newVehicle, modelYear: e.target.value })} required />
                                                        </div>
                                                        <div className="form-group">
                                                            <label className="form-label">Chassis Number</label>
                                                            <input className="form-input" value={newVehicle.chassisNumber} onChange={e => setNewVehicle({ ...newVehicle, chassisNumber: e.target.value })} required />
                                                        </div>
                                                    </div>
                                                    <button type="submit" className="btn btn-primary">Save Vehicle</button>
                                                </form>
                                            </div>
                                        )}

                                        <div className="vehicle-list">
                                            {vehicles.length === 0 ? (
                                                <p>No vehicles found.</p>
                                            ) : (
                                                <table className="data-table">
                                                    <thead>
                                                        <tr>
                                                            <th>Plate</th>
                                                            <th>Brand</th>
                                                            <th>Model</th>
                                                            <th>Actions</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {vehicles.map(v => {
                                                            const hasAppointment = appointments.some(a => a.vehicle && a.vehicle.plateCode === v.plateCode);
                                                            return (
                                                                <tr key={v.id}>
                                                                    <td>{v.plateCode}</td>
                                                                    <td>{v.brand ? v.brand.name : '-'}</td>
                                                                    <td>{v.modelYear}</td>
                                                                    <td>
                                                                        <div className="action-buttons">
                                                                            <button
                                                                                className="btn btn-sm btn-success"
                                                                                onClick={() => handleCreateAppointment(v.plateCode)}
                                                                            >
                                                                                New Appointment
                                                                            </button>
                                                                            {hasAppointment && (
                                                                                <button
                                                                                    className="btn btn-sm btn-info"
                                                                                    onClick={() => handleShowDetails(v.plateCode)}
                                                                                    style={{ marginLeft: '10px' }}
                                                                                >
                                                                                    Show Details
                                                                                </button>
                                                                            )}
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                            );
                                                        })}
                                                    </tbody>
                                                </table>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'stations' && (
                        <div>
                            <div style={{ marginBottom: '20px' }}>
                                <button className="btn btn-primary" onClick={() => setShowAddStation(!showAddStation)}>
                                    {showAddStation ? 'Cancel' : 'Add New Station'}
                                </button>
                            </div>

                            {showAddStation && (
                                <div className="card">
                                    <h3>New Station</h3>
                                    <form onSubmit={handleCreateStation}>
                                        <div className="form-grid">
                                            <div className="form-group">
                                                <label className="form-label">Station Code</label>
                                                <input className="form-input" value={newStation.stationCode} onChange={e => setNewStation({ ...newStation, stationCode: e.target.value })} required />
                                            </div>
                                            <div className="form-group">
                                                <label className="form-label">Capacity</label>
                                                <input type="number" className="form-input" value={newStation.capacity} onChange={e => setNewStation({ ...newStation, capacity: parseInt(e.target.value) })} required />
                                            </div>
                                            <div className="form-group">
                                                <label className="form-label">Status</label>
                                                <select className="form-input" value={newStation.open} onChange={e => setNewStation({ ...newStation, open: e.target.value === 'true' })}>
                                                    <option value="true">Open</option>
                                                    <option value="false">Closed</option>
                                                </select>
                                            </div>
                                            <div className="form-group">
                                                <label className="form-label">Exclusive Brand (Optional)</label>
                                                <select
                                                    className="form-input"
                                                    value={newStation.exclusiveBrand ? newStation.exclusiveBrand.id : ''}
                                                    onChange={e => {
                                                        const val = e.target.value;
                                                        if (val === "") {
                                                            setNewStation({ ...newStation, exclusiveBrand: null });
                                                        } else {
                                                            const selectedBrand = brands.find(b => b.id.toString() === val);
                                                            setNewStation({ ...newStation, exclusiveBrand: selectedBrand });
                                                        }
                                                    }}
                                                >
                                                    <option value="">None</option>
                                                    {brands.map(b => (
                                                        <option key={b.id} value={b.id}>{b.name}</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                        <button type="submit" className="btn btn-primary">Create Station</button>
                                    </form>
                                </div>
                            )}

                            <div className="stations-grid">
                                {stations.map(station => (
                                    <div key={station.id} className={`card station-card ${station.open ? 'open' : 'closed'}`}>
                                        {editingStation && editingStation.id === station.id ? (
                                            <form onSubmit={handleUpdateStation}>
                                                <div className="form-group">
                                                    <label>Code</label>
                                                    <input className="form-input" value={editingStation.stationCode} onChange={e => setEditingStation({ ...editingStation, stationCode: e.target.value })} required />
                                                </div>
                                                <div className="form-group">
                                                    <label>Capacity</label>
                                                    <input type="number" className="form-input" value={editingStation.capacity} onChange={e => setEditingStation({ ...editingStation, capacity: parseInt(e.target.value) })} required />
                                                </div>
                                                <div className="form-group">
                                                    <label>Status</label>
                                                    <select className="form-input" value={editingStation.open} onChange={e => setEditingStation({ ...editingStation, open: e.target.value === 'true' })}>
                                                        <option value="true">Open</option>
                                                        <option value="false">Closed</option>
                                                    </select>
                                                </div>
                                                <div className="form-group">
                                                    <label>Exclusive Brand</label>
                                                    <select
                                                        className="form-input"
                                                        value={editingStation.exclusiveBrand ? editingStation.exclusiveBrand.id : ''}
                                                        onChange={e => {
                                                            const val = e.target.value;
                                                            if (val === "") {
                                                                setEditingStation({ ...editingStation, exclusiveBrand: null });
                                                            } else {
                                                                const selectedBrand = brands.find(b => b.id.toString() === val);
                                                                setEditingStation({ ...editingStation, exclusiveBrand: selectedBrand });
                                                            }
                                                        }}
                                                    >
                                                        <option value="">None</option>
                                                        {brands.map(b => (
                                                            <option key={b.id} value={b.id}>{b.name}</option>
                                                        ))}
                                                    </select>
                                                </div>
                                                <div style={{ marginTop: '10px' }}>
                                                    <button type="submit" className="btn btn-sm btn-success">Save</button>
                                                    <button type="button" className="btn btn-sm btn-secondary" onClick={() => setEditingStation(null)} style={{ marginLeft: '5px' }}>Cancel</button>
                                                </div>
                                            </form>
                                        ) : (
                                            <>
                                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                                                    <h3>{station.stationCode}</h3>
                                                    <button className="btn btn-sm btn-info" onClick={() => setEditingStation(station)}>Edit</button>
                                                </div>
                                                <p><strong>Status:</strong> {station.open ? 'Open' : 'Closed'}</p>
                                                <p><strong>Capacity:</strong> {station.capacity}</p>
                                                {station.exclusiveBrand && (
                                                    <p className="exclusive-badge">Only {station.exclusiveBrand.name}</p>
                                                )}
                                            </>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Modal */}
            {showModal && selectedAppointment && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h3>Appointment Details</h3>
                            <button className="close-btn" onClick={closeModal}>&times;</button>
                        </div>
                        <div className="modal-body">
                            <p><strong>Appointment ID:</strong> {selectedAppointment.id}</p>
                            <div className="form-group" style={{ margin: '10px 0' }}>
                                <label><strong>Status:</strong></label>
                                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginTop: '5px' }}>
                                    <select
                                        className="form-input"
                                        style={{ width: 'auto' }}
                                        value={editStatus}
                                        onChange={(e) => setEditStatus(e.target.value)}
                                    >
                                        <option value="PENDING">PENDING</option>
                                        <option value="COMPLETED">COMPLETED</option>
                                        <option value="CANCELLED">CANCELLED</option>
                                    </select>
                                    <button className="btn btn-sm btn-success" onClick={handleUpdateStatus}>Update</button>
                                </div>
                            </div>
                            <p><strong>Vehicle:</strong> {selectedAppointment.vehicle ? selectedAppointment.vehicle.plateCode : 'N/A'}</p>
                            <p><strong>Assigned Station:</strong> {selectedAppointment.station ? selectedAppointment.station.stationCode : 'Pending'}</p>
                            {selectedAppointment.station && selectedAppointment.station.exclusiveBrand && (
                                <p><strong>Station Type:</strong> Exclusive ({selectedAppointment.station.exclusiveBrand.name})</p>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-primary" onClick={closeModal}>Close</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SecretaryDashboard;
