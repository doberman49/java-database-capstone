## MySQL Database Design

### Table: patients
- patient_id: INT, Primary key, Auto Increment
- patient_name: STRING
- patiend_age: INT
- patiend_genre: CHAR (M=male, F=female, O=other)
- patiend_phone: STRING
- patiend_address: STRING

### Table: doctors
- doctor_id: INT, Primary Key, Auto Increment
- doctor_name: STRING
- doctor_speciality: STRING
- doctor_phone: STRING
- doctor_status: CHAR (A=active, I=inactive)

### Table: appointments
- appointment_id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- appointment_status: CHAR (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- payment_id: INT, Foreing key -> payments (id)
- appointment_clinic: INT, Foreign Key -> clinics (id)

### Table: admin
- admin_user: STRING, Primary key
- admin_name: STRING
- admin_rol: CHAR (A=administrator, D=doctor, O=operator)
- admin_status: CHAR (A=active, I=inactive)
- admin_time: DATETIME

### Table: clinics
- clinic_id: INT, Primary Key, Auto Increment
- clinic_name: STRING
- clinic_phone: STRING
- clinic_adress: STRING
- clinic_status: CHAR (A=active, I=inactive)

### Table: payments
- payment_id: INT, Primary Key, Auto Increment
- payment_amount: FLOAT
- payment_status: CHAR (0=due, 1=settled, 2=cancelled)
- payment_time: DATETIME, Not Null


## MongoDB Collection Design

prescriptions

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

messages

{
  "message_id": "12345",
  "origin": "67890",
  "origin_type": "Operator",
  "destiny": "111213",
  "destiny_type": "Doctor",
  "content_type": "Remember",
  "content": "A request…"
  "createdAt": "2023-10-02T12:00:00Z"
}


