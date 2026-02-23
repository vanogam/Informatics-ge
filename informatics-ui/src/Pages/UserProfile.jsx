import React, { useContext, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Box, Typography, Card, CardContent, Tabs, Tab, CircularProgress } from '@mui/material'
import { AxiosContext } from '../utils/axiosInstance'
import { AuthContext } from '../store/authentication'
import SubmissionsList from '../Components/SubmissionsList'
import ChangePassword from '../Components/ChangePassword'

export default function UserProfile() {
    const { userId } = useParams()
    const axiosInstance = useContext(AxiosContext)
    const authContext = useContext(AuthContext)
    const [profile, setProfile] = useState(null)
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [tabValue, setTabValue] = useState(0)
    const [currentUserId, setCurrentUserId] = useState(null)

    useEffect(() => {
        axiosInstance.get('/user')
            .then((currentUserResponse) => {
                const currentId = currentUserResponse.data.id
                setCurrentUserId(currentId)
                
                const targetUserId = userId || currentId
                
                return Promise.all([
                    axiosInstance.get(`/user/${targetUserId}`),
                    axiosInstance.get(`/user/${targetUserId}/profile`)
                ])
            })
            .then(([userResponse, profileResponse]) => {
                setUser(userResponse.data)
                setProfile(profileResponse.data)
                setLoading(false)
            })
            .catch((error) => {
                console.error('Error fetching user data:', error)
                setLoading(false)
            })
    }, [userId, axiosInstance])

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue)
    }

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A'
        const date = new Date(dateString)
        return date.toLocaleString('ka-GE', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
    }

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
            </Box>
        )
    }

    if (!profile) {
        return (
            <Box sx={{ padding: '2rem' }}>
                <Typography variant="h5">Profile not found</Typography>
            </Box>
        )
    }

    const targetUserId = userId ? Number(userId) : (user?.id || currentUserId)
    const isOwnProfile = currentUserId && targetUserId && targetUserId === currentUserId

    return (
        <Box sx={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
            <Card sx={{ marginBottom: '2rem' }}>
                <CardContent>
                    <Typography variant="h4" component="h1" gutterBottom>
                        {user ? `${user.firstName} ${user.lastName}` : profile.username}
                    </Typography>
                    {user && (
                        <Typography variant="body2" color="text.secondary" sx={{ marginTop: '0.5rem' }}>
                            @{user.username}
                        </Typography>
                    )}
                    <Typography variant="body1" color="text.secondary" sx={{ marginTop: '1rem' }}>
                        <strong>ამოხსნილი ამოცანები:</strong> {profile.solvedProblemsCount}
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ marginTop: '0.5rem' }}>
                        <strong>ბოლო ავტორიზაცია:</strong> {formatDate(profile.lastLogin)}
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ marginTop: '0.5rem' }}>
                        <strong>რეგისტრაციის თარიღი:</strong> {formatDate(profile.registrationTime)}
                    </Typography>
                </CardContent>
            </Card>

            <Card>
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={tabValue} onChange={handleTabChange}>
                        <Tab label="მცდელობები" />
                        {isOwnProfile && <Tab label="პაროლის შეცვლა" />}
                    </Tabs>
                </Box>
                <Box sx={{ padding: '1rem' }}>
                    {tabValue === 0 && (
                        <SubmissionsList
                            getEndpoint={() => `/user/${targetUserId}/submissions`}
                            title=""
                        />
                    )}
                    {tabValue === 1 && isOwnProfile && (
                        <ChangePassword />
                    )}
                </Box>
            </Card>
        </Box>
    )
}

