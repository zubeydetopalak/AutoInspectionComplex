import React, { useState, useEffect } from 'react';
import {
    getAppointments,
    createInspection,
    getInspections,
    updateInspection,
    completeInspection,
    getChecklistTemplates
} from '../services/api';

const InspectorDashboard = () => {
    const [appointments, setAppointments] = useState([]);
    const [filteredAppointments, setFilteredAppointments] = useState([]);
    const [inspections, setInspections] = useState([]);
    const [searchPlate, setSearchPlate] = useState('');
    const [showAllAppointments, setShowAllAppointments] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [currentUser, setCurrentUser] = useState(null);

    // Modal State
    const [showModal, setShowModal] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [currentInspection, setCurrentInspection] = useState(null);
    const [checklist, setChecklist] = useState([]);
    const [addedDetails, setAddedDetails] = useState([]);
    const [newDetail, setNewDetail] = useState({ templateId: '', passed: true, note: '' });

    useEffect(() => {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            setCurrentUser(JSON.parse(userStr));
        }
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [appRes, inspRes, checkRes] = await Promise.all([
                getAppointments(),
                getInspections(),
                getChecklistTemplates()
            ]);
            setAppointments(appRes.data);
            setInspections(inspRes.data);
            setChecklist(checkRes.data);

            // Re-apply filter if needed
            if (searchPlate.trim()) {
                const filtered = appRes.data.filter(a =>
                    a.vehicle && a.vehicle.plateCode.toLowerCase() === searchPlate.toLowerCase()
                );
                setFilteredAppointments(filtered);
            } else {
                setFilteredAppointments(appRes.data);
            }
        } catch (error) {
            console.error("Error loading data", error);
            setMessage({ text: 'Failed to load data.', type: 'error' });
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setShowAllAppointments(false);
        filterAppointments(searchPlate);
    };

    const handleShowAll = () => {
        setShowAllAppointments(true);
        setSearchPlate('');
        setFilteredAppointments(appointments);
    };

    const filterAppointments = (plate) => {
        if (!plate.trim()) {
            setFilteredAppointments(appointments);
            return;
        }
        const filtered = appointments.filter(a =>
            a.vehicle && a.vehicle.plateCode.toLowerCase() === plate.toLowerCase()
        );
        setFilteredAppointments(filtered);
    };

    const openInspectionModal = async (appointment) => {
        setSelectedAppointment(appointment);

        // Check if inspection exists
        let existing = inspections.find(i => i.appointment && i.appointment.id === appointment.id);

        if (!existing) {
            try {
                const inspection = {
                    appointment: { id: appointment.id },
                    inspectionDate: new Date().toISOString(),
                    result: 'PENDING',
                    notes: 'Inspection started',
                    inspector: currentUser ? { id: currentUser.id } : null
                };
                const response = await createInspection(inspection);
                existing = response.data;
                setInspections(prev => [...prev, existing]);
                setMessage({ text: 'Inspection started!', type: 'success' });
            } catch (error) {
                console.error("Error creating inspection", error);
                setMessage({ text: 'Failed to start inspection.', type: 'error' });
                return;
            }
        }

        setCurrentInspection(existing);
        setAddedDetails(existing.details || []);
        setNewDetail({ templateId: '', passed: true, note: '' });
        setShowModal(true);
    };

    const handleAddDetail = () => {
        if (!newDetail.templateId) {
            alert("Please select a checklist item.");
            return;
        }
        const template = checklist.find(c => c.id === parseInt(newDetail.templateId));
        if (!template) return;

        const detail = {
            checkItem: template,
            passed: newDetail.passed,
            inspectorNote: newDetail.note
        };

        setAddedDetails([...addedDetails, detail]);
        setNewDetail({ templateId: '', passed: true, note: '' });
    };

    const handleRemoveDetail = (index) => {
        const updated = [...addedDetails];
        updated.splice(index, 1);
        setAddedDetails(updated);
    };

    const handleUpdateInspection = async () => {
        if (!currentInspection) return;
        try {
            const updated = { ...currentInspection, notes: currentInspection.notes, details: addedDetails };
            await updateInspection(currentInspection.id, updated);

            setMessage({ text: 'Inspection updated!', type: 'success' });

            // Fetch fresh data to ensure we have the latest state (including generated IDs for details)
            const [inspRes, appRes] = await Promise.all([
                getInspections(),
                getAppointments()
            ]);

            setInspections(inspRes.data);
            setAppointments(appRes.data);

            closeModal();
        } catch (error) {
            console.error("Error updating inspection", error);
            setMessage({ text: 'Failed to update inspection.', type: 'error' });
        }
    };

    const handleCompleteInspection = async () => {
        if (!currentInspection) return;

        try {
            await completeInspection(currentInspection.id, addedDetails);
            setMessage({ text: 'Inspection completed!', type: 'success' });
            setShowModal(false);
            loadData();
        } catch (error) {
            console.error("Error completing inspection", error);
            setMessage({ text: 'Failed to complete inspection.', type: 'error' });
        }
    };

    const closeModal = () => {
        setShowModal(false);
        setSelectedAppointment(null);
        setCurrentInspection(null);
        setAddedDetails([]);
    };

    return (
        <div className="dashboard-container">
            <div className="sidebar">
                <div className="sidebar-header">
                    <h3>Auto Service</h3>
                    <p>Inspector Portal</p>
                </div>
                <ul className="sidebar-menu">
                    <li className="active">Inspections</li>
                </ul>
            </div>

            <div className="main-content">
                <header className="top-bar">
                    <h2>Inspection Management</h2>
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

                    <div className="card search-card">
                        <h3>Find Appointment</h3>
                        <form onSubmit={handleSearch} className="search-form">
                            <div className="form-group">
                                <input
                                    type="text"
                                    className="form-input"
                                    placeholder="Search by Plate Code"
                                    value={searchPlate}
                                    onChange={(e) => {
                                        setSearchPlate(e.target.value);
                                        filterAppointments(e.target.value);
                                    }}
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Search</button>
                            <button type="button" className="btn btn-secondary" onClick={handleShowAll} style={{ marginLeft: '10px' }}>Show All</button>
                        </form>
                    </div>

                    <div className="card">
                        <h3>Appointments</h3>
                        <div className="vehicle-list">
                            {filteredAppointments.length === 0 ? (
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
                                        {filteredAppointments.map(app => {
                                            const insp = inspections.find(i => i.appointment && i.appointment.id === app.id);
                                            return (
                                                <tr key={app.id}>
                                                    <td>{app.id}</td>
                                                    <td>{app.vehicle ? app.vehicle.plateCode : 'N/A'}</td>
                                                    <td><span className={`status-badge ${app.status}`}>{app.status}</span></td>
                                                    <td>{app.station ? app.station.stationCode : 'Pending'}</td>
                                                    <td>
                                                        <button
                                                            className={`btn btn-sm ${insp ? (insp.result === 'PENDING' ? 'btn-info' : 'btn-secondary') : 'btn-success'}`}
                                                            onClick={() => openInspectionModal(app)}
                                                        >
                                                            {!insp ? 'Start Inspection' : (insp.result === 'PENDING' ? 'Update Inspection' : 'Show Inspection')}
                                                        </button>
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
            </div>

            {/* Inspection Modal */}
            {showModal && selectedAppointment && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '800px', width: '90%', maxHeight: '90vh', display: 'flex', flexDirection: 'column' }}>
                        <div className="modal-header">
                            <h3>Inspection for {selectedAppointment.vehicle.plateCode}</h3>
                            <button className="close-btn" onClick={closeModal}>&times;</button>
                        </div>
                        <div className="modal-body" style={{ overflowY: 'auto', flex: 1 }}>
                            {currentInspection && (
                                <div>
                                    <div className="info-card" style={{ marginBottom: '20px' }}>
                                        <p><strong>Inspection ID:</strong> {currentInspection.id}</p>
                                        <p><strong>Date:</strong> {new Date(currentInspection.inspectionDate).toLocaleString()}</p>
                                        {currentInspection.result && currentInspection.result !== 'PENDING' && (
                                            <p><strong>Result:</strong> {currentInspection.result}</p>
                                        )}
                                        {currentInspection.inspector && (
                                            <p><strong>Inspector:</strong> {currentInspection.inspector.name}</p>
                                        )}
                                    </div>

                                    <h4>Inspection Details</h4>

                                    {/* List of Added Details */}
                                    <div className="checklist-container" style={{ marginBottom: '20px' }}>
                                        <table className="data-table">
                                            <thead>
                                                <tr>
                                                    <th>Item</th>
                                                    <th>Status</th>
                                                    <th>Note</th>
                                                    <th>Action</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {addedDetails.length === 0 ? (
                                                    <tr><td colSpan="4">No details added yet.</td></tr>
                                                ) : (
                                                    addedDetails.map((detail, index) => (
                                                        <tr key={index}>
                                                            <td>{detail.checkItem ? detail.checkItem.description : 'Unknown'}</td>
                                                            <td style={{ color: detail.passed ? 'green' : 'red', fontWeight: 'bold' }}>
                                                                {detail.passed ? 'PASS' : 'FAIL'}
                                                            </td>
                                                            <td>{detail.inspectorNote}</td>
                                                            <td>
                                                                {(!currentInspection.result || currentInspection.result === 'PENDING') && (
                                                                    <button className="btn btn-sm btn-danger" onClick={() => handleRemoveDetail(index)}>Remove</button>
                                                                )}
                                                            </td>
                                                        </tr>
                                                    ))
                                                )}
                                            </tbody>
                                        </table>
                                    </div>

                                    {/* Add New Detail Form */}
                                    {(!currentInspection.result || currentInspection.result === 'PENDING') && (
                                        <div className="card" style={{ padding: '15px', background: '#f9f9f9' }}>
                                            <h5>Add Inspection Detail</h5>
                                            <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
                                                <div style={{ flex: 2, minWidth: '200px' }}>
                                                    <label>Checklist Item:</label>
                                                    <select
                                                        className="form-input"
                                                        value={newDetail.templateId}
                                                        onChange={(e) => setNewDetail({ ...newDetail, templateId: e.target.value })}
                                                    >
                                                        <option value="">Select Item...</option>
                                                        {checklist.map(t => (
                                                            <option key={t.id} value={t.id}>{t.description} ({t.level})</option>
                                                        ))}
                                                    </select>
                                                </div>
                                                <div style={{ flex: 1, minWidth: '100px' }}>
                                                    <label>Status:</label>
                                                    <select
                                                        className="form-input"
                                                        value={newDetail.passed}
                                                        onChange={(e) => setNewDetail({ ...newDetail, passed: e.target.value === 'true' })}
                                                    >
                                                        <option value="true">PASS</option>
                                                        <option value="false">FAIL</option>
                                                    </select>
                                                </div>
                                                <div style={{ flex: 2, minWidth: '200px' }}>
                                                    <label>Note:</label>
                                                    <input
                                                        type="text"
                                                        className="form-input"
                                                        placeholder="Inspector Note"
                                                        value={newDetail.note}
                                                        onChange={(e) => setNewDetail({ ...newDetail, note: e.target.value })}
                                                    />
                                                </div>
                                                <button className="btn btn-primary" onClick={handleAddDetail}>Add</button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={closeModal}>
                                {currentInspection && currentInspection.result !== 'PENDING' ? 'Close' : 'Cancel'}
                            </button>
                            {currentInspection && (!currentInspection.result || currentInspection.result === 'PENDING') && (
                                <>
                                    <button className="btn btn-info" onClick={handleUpdateInspection} style={{ marginRight: '10px' }}>Kaydet</button>
                                    <button className="btn btn-success" onClick={handleCompleteInspection}>Complete Inspection</button>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default InspectorDashboard;
