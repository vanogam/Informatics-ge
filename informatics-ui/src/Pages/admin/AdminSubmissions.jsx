import {useState} from 'react'
import {Box, CircularProgress} from '@mui/material'
import AdminNavigationBar from '../../Components/AdminNavigationBar'
import SubmissionsList from '../../Components/SubmissionsList'
import SubmissionFilters, {EMPTY_FILTERS} from '../../Components/SubmissionFilters'
import {useRequireAdmin} from '../../utils/useRequireAdmin'

export default function AdminSubmissions() {
    const {ready} = useRequireAdmin()
    const [filters, setFilters] = useState(EMPTY_FILTERS)

    const getEndpoint = () => '/admin/submissions'

    if (!ready) {
        return (
            <>
                <AdminNavigationBar />
                <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                    <CircularProgress />
                </Box>
            </>
        )
    }

    return (
        <main>
            <AdminNavigationBar />
            <Box sx={{padding: '0 1rem'}}>
                <SubmissionFilters
                    value={filters}
                    onChange={setFilters}
                    showUsername
                    showContest
                    adminTasks
                />
                <SubmissionsList
                    getEndpoint={getEndpoint}
                    title="ყველა მცდელობა"
                    filters={filters}
                />
            </Box>
        </main>
    )
}
