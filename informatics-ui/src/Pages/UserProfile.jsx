import React, { useContext, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Box, Typography, Card, CardContent, Tabs, Tab, CircularProgress } from '@mui/material'
import { AxiosContext } from '../utils/axiosInstance'
import { AuthContext } from '../store/authentication'
import SubmissionsList from '../Components/SubmissionsList'
import ChangePassword from '../Components/ChangePassword'

export default function UserProfile() {
    const { username } = useParams()
    const axiosInstance = useContext(AxiosContext)
    const authContext = useContext(AuthContext)
    const [profile, setProfile] = useState(null)
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [tabValue, setTabValue] = useState(0)
    const [currentUserUsername, setCurrentUserUsername] = useState(null)
    const [targetUsername, setTargetUsername] = useState(null)

    useEffect(() => {
        axiosInstance.get('/user')
            .then((currentUserResponse) => {
                const currentUsername = currentUserResponse.data.username
                setCurrentUserUsername(currentUsername)
                
                const resolvedTargetUsername = username || currentUsername
                setTargetUsername(resolvedTargetUsername)
                
                return Promise.all([
                    axiosInstance.get(`/user/username/${resolvedTargetUsername}`),
                    axiosInstance.get(`/user/username/${resolvedTargetUsername}/profile`)
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
    }, [username, axiosInstance])

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

    const resolvedTargetUsername = targetUsername || user?.username || currentUserUsername
    const isOwnProfile = currentUserUsername && resolvedTargetUsername && resolvedTargetUsername === currentUserUsername

    return (
        <Box sx={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
            <Card>
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={tabValue} onChange={handleTabChange}>
                        <Tab label="Information" />
                        <Tab label="მცდელობები" />
                        {isOwnProfile && <Tab label="პაროლის შეცვლა" />}
                    </Tabs>
                </Box>
                <Box sx={{ padding: '1rem' }}>
                    {tabValue === 0 && (
                        <Card elevation={0}>
                            <CardContent>
                                {user && (
                                    <>
                                        <Typography variant="h4" component="h1" gutterBottom>
                                            {profile.username}
                                        </Typography>
                                        <Typography variant="h5" component="h2" gutterBottom>
                                            {`${user.firstName} ${user.lastName}`}
                                        </Typography>
                                    </>
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
                    )}

                    {tabValue === 1 && resolvedTargetUsername && (
                        <SubmissionsList
                            getEndpoint={() => `/user/username/${resolvedTargetUsername}/submissions`}
                            title=""
                            autoRefresh={false}
                        />
                    )}

                    {tabValue === 2 && isOwnProfile && (
                        <ChangePassword />
                    )}
                </Box>
            </Card>
        </Box>
    )
}

