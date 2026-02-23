import React, { useContext, useState, useRef } from 'react'
import { Box, Button, TextField, Typography, Alert } from '@mui/material'
import { AxiosContext } from '../utils/axiosInstance'
import { toast } from 'react-toastify'
import getMessage from './lang'

export default function ChangePassword() {
    const axiosInstance = useContext(AxiosContext)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState(false)
    const oldPasswordRef = useRef('')
    const newPasswordRef = useRef('')
    const confirmPasswordRef = useRef('')

    const handleSubmit = () => {
        setError('')
        setSuccess(false)

        const oldPassword = oldPasswordRef.current.value
        const newPassword = newPasswordRef.current.value
        const confirmPassword = confirmPasswordRef.current.value

        if (!oldPassword || !newPassword || !confirmPassword) {
            setError('გთხოვთ შეავსოთ ყველა ველი')
            return
        }

        if (newPassword !== confirmPassword) {
            setError('ახალი პაროლები არ ემთხვევა')
            return
        }

        if (newPassword.length < 6) {
            setError('ახალი პაროლი უნდა იყოს მინიმუმ 6 სიმბოლო')
            return
        }

        setLoading(true)
        axiosInstance.post('/user/change-password', {
            oldPassword: oldPassword,
            newPassword: newPassword
        })
            .then((response) => {
                setSuccess(true)
                oldPasswordRef.current.value = ''
                newPasswordRef.current.value = ''
                confirmPasswordRef.current.value = ''
                toast.success(getMessage('ka', 'passwordChanged') || 'პაროლი წარმატებით შეიცვალა')
            })
            .catch((error) => {
                const errorMessage = error.response?.data?.message || 'პაროლის შეცვლა ვერ მოხერხდა'
                setError(errorMessage)
                toast.error(errorMessage)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    return (
        <Box sx={{ maxWidth: '500px', margin: '0 auto', padding: '2rem' }}>
            <Typography variant="h6" gutterBottom>
                პაროლის შეცვლა
            </Typography>
            {error && (
                <Alert severity="error" sx={{ marginBottom: '1rem' }}>
                    {error}
                </Alert>
            )}
            {success && (
                <Alert severity="success" sx={{ marginBottom: '1rem' }}>
                    პაროლი წარმატებით შეიცვალა
                </Alert>
            )}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <TextField
                    label="მიმდინარე პაროლი"
                    type="password"
                    inputRef={oldPasswordRef}
                    fullWidth
                    required
                />
                <TextField
                    label="ახალი პაროლი"
                    type="password"
                    inputRef={newPasswordRef}
                    fullWidth
                    required
                />
                <TextField
                    label="დაადასტურეთ ახალი პაროლი"
                    type="password"
                    inputRef={confirmPasswordRef}
                    fullWidth
                    required
                />
                <Button
                    variant="contained"
                    onClick={handleSubmit}
                    disabled={loading}
                    sx={{ marginTop: '1rem' }}
                >
                    {loading ? 'მიმდინარეობს...' : 'პაროლის შეცვლა'}
                </Button>
            </Box>
        </Box>
    )
}


