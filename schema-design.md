## MySQL Database Design

patients

doctors

appointments
### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

admin

clinic_locations

payments


## MongoDB Collection Design

prescripciones, comentarios, registros o mensajes

{
  "prescriptionId": "12345",
  "patientId": "67890",
  "doctorId": "54321",
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Every 8 hours",
      "duration": "7 days"
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "frequency": "Every 6 hours",
      "duration": "3 days"
    }
  ],
  "tags": ["antibiotic", "pain relief"],
  "metadata": {
    "createdAt": "2023-10-01T12:00:00Z",
    "updatedAt": "2023-10-02T12:00:00Z",
    "status": "active"
  },
  "notes": "Patient should take medication with food."
}

### Collection: prescriptions
```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Tome 1 tableta cada 6 horas.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}

