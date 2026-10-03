import React, { useContext, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Box, Typography, Card, CardContent, Tabs, Tab, CircularProgress, Button } from '@mui/material'
import { AxiosContext } from '../utils/axiosInstance'
import { AuthContext } from '../store/authentication'
import SubmissionsList from '../Components/SubmissionsList'
import SubmissionFilters, {EMPTY_FILTERS} from '../Components/SubmissionFilters'
import EditProfileDialog from '../Components/EditProfileDialog'

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
    const [submissionFilters, setSubmissionFilters] = useState(EMPTY_FILTERS)
    const [editOpen, setEditOpen] = useState(false)

    useEffect(() => {
        setLoading(true)
        setTargetUsername(username)

        // Who's logged in (if anyone) only decides whether to show "my own profile" affordances
        // like the edit-profile button - it must not gate loading the profile itself, or an
        // anonymous visitor could never view anyone's public profile page.
        axiosInstance.get('/user') // interceptor already silences 401s for this endpoint
            .then((response) => setCurrentUserUsername(response.data.username))
            .catch(() => setCurrentUserUsername(null))

        Promise.all([
            // Carries the viewed user's email, so unlike the profile/submissions endpoints it's
            // still auth-gated server-side; ignoreErrors keeps an expected anonymous 401 from
            // popping the global "please log in" toast. The name line is skipped when this fails.
            axiosInstance.get(`/user/username/${username}`, {ignoreErrors: true}).catch(() => null),
            axiosInstance.get(`/user/username/${username}/profile`)
        ])
            .then(([userResponse, profileResponse]) => {
                setUser(userResponse?.data ?? null)
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
                    </Tabs>
                </Box>
                <Box sx={{ padding: '1rem' }}>
                    {tabValue === 0 && (
                        <Card elevation={0}>
                            <CardContent>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                    {user && (
                                        <Box>
                                            <Typography variant="h4" component="h1" gutterBottom>
                                                {profile.username}
                                            </Typography>
                                            <Typography variant="h5" component="h2" gutterBottom>
                                                {`${user.firstName} ${user.lastName}`}
                                            </Typography>
                                        </Box>
                                    )}
                                    {isOwnProfile && (
                                        <Button variant="outlined" onClick={() => setEditOpen(true)}>
                                            რედაქტირება
                                        </Button>
                                    )}
                                </Box>
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
                        <Box sx={{minWidth: 0, width: '100%'}}>
                            <SubmissionFilters
                                value={submissionFilters}
                                onChange={setSubmissionFilters}
                                showContest
                            />
                            <SubmissionsList
                                getEndpoint={() => `/user/username/${resolvedTargetUsername}/submissions`}
                                title=""
                                autoRefresh={false}
                                filters={submissionFilters}
                            />
                        </Box>
                    )}
                </Box>
            </Card>

            {isOwnProfile && (
                <EditProfileDialog
                    open={editOpen}
                    onClose={() => setEditOpen(false)}
                    user={user}
                    onSaved={(updated) => {
                        setUser((prev) => ({ ...prev, ...updated }))
                        setEditOpen(false)
                    }}
                />
            )}
        </Box>
    )
}

