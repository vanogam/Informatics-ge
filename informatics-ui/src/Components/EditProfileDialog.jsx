import React, { useContext, useEffect, useState } from 'react'
import {
    Dialog,
    DialogTitle,
    DialogContent,
    Box,
    Paper,
    TextField,
    Button,
    Alert,
    IconButton,
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import { AxiosContext } from '../utils/axiosInstance'
import { toast } from 'react-toastify'
import getMessage from './lang'
import ChangePassword from './ChangePassword'

export default function EditProfileDialog({ open, onClose, user, onSaved }) {
    const axiosInstance = useContext(AxiosContext)
    const [email, setEmail] = useState('')
    const [firstName, setFirstName] = useState('')
    const [lastName, setLastName] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        if (open) {
            setEmail(user?.email || '')
            setFirstName(user?.firstName || '')
            setLastName(user?.lastName || '')
            setError('')
        }
    }, [open, user])

    const handleSave = () => {
        setError('')

        const trimmedEmail = email.trim()
        const trimmedFirstName = firstName.trim()
        const trimmedLastName = lastName.trim()

        if (!trimmedEmail || !trimmedFirstName || !trimmedLastName) {
            setError('გთხოვთ შეავსოთ ყველა ველი')
            return
        }

        setLoading(true)
        axiosInstance.put('/user/profile', {
            email: trimmedEmail,
            firstName: trimmedFirstName,
            lastName: trimmedLastName,
        })
            .then(() => {
                toast.success('პროფილი წარმატებით განახლდა')
                onSaved && onSaved({ email: trimmedEmail, firstName: trimmedFirstName, lastName: trimmedLastName })
            })
            .catch((err) => {
                const errorMessage = err.response?.data?.message || 'პროფილის განახლება ვერ მოხერხდა'
                setError(errorMessage)
                toast.error(errorMessage)
            })
            .finally(() => {
                setLoading(false)
            })
    }

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                პროფილის რედაქტირება
                <IconButton onClick={onClose} size="small">
                    <CloseIcon fontSize="small" />
                </IconButton>
            </DialogTitle>
            <DialogContent>
                <Paper variant="outlined" sx={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
                    {error && (
                        <Alert severity="error" sx={{ marginBottom: '1rem' }}>
                            {error}
                        </Alert>
                    )}
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <TextField
                            label="მომხმარებელი"
                            value={user?.username || ''}
                            disabled
                            fullWidth
                        />
                        <TextField
                            label="ელ. ფოსტა"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            fullWidth
                            required
                        />
                        <TextField
                            label="სახელი"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            fullWidth
                            required
                        />
                        <TextField
                            label="გვარი"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                            fullWidth
                            required
                        />
                        <Button
                            variant="contained"
                            onClick={handleSave}
                            disabled={loading}
                            sx={{ marginTop: '0.5rem' }}
                        >
                            {loading ? 'მიმდინარეობს...' : getMessage('ka', 'save')}
                        </Button>
                    </Box>
                </Paper>

                <Paper variant="outlined" sx={{ padding: '1.5rem' }}>
                    <ChangePassword />
                </Paper>
            </DialogContent>
        </Dialog>
    )
}
