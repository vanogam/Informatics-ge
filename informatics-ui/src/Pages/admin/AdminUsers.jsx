import {useContext, useEffect, useState} from 'react'
import {toast} from 'react-toastify'
import {
    Box,
    CircularProgress,
    Container,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Chip,
    IconButton,
    Tooltip,
    TextField,
} from '@mui/material'
import VisibilityIcon from '@mui/icons-material/Visibility'
import BlockIcon from '@mui/icons-material/Block'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import AdminNavigationBar from '../../Components/AdminNavigationBar'
import PaginationControls from '../../Components/PaginationControls'
import {usePagination} from '../../utils/usePagination'
import {useRequireAdmin} from '../../utils/useRequireAdmin'
import {AxiosContext} from '../../utils/axiosInstance'
import {useConfirmDialog} from '../../utils/ConfirmDialogContext'
import getMessage from '../../Components/lang'

const SEARCH_DEBOUNCE_MS = 400

function formatLastLogin(dateString) {
    if (!dateString) {
        return getMessage('ka', 'adminUsersLastLoginNever')
    }
    return new Date(dateString).toLocaleString('ka-GE', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    })
}

export default function AdminUsers() {
    const axiosInstance = useContext(AxiosContext)
    const {showConfirmDialog} = useConfirmDialog()
    const {ready} = useRequireAdmin()
    const pagination = usePagination()
    const {offset, pageSize, setTotalCount} = pagination

    const [users, setUsers] = useState([])
    const [loading, setLoading] = useState(true)
    const [searchDraft, setSearchDraft] = useState('')
    const [search, setSearch] = useState('')

    useEffect(() => {
        const timeout = setTimeout(() => {
            setSearch(searchDraft)
            pagination.resetPage()
        }, SEARCH_DEBOUNCE_MS)
        return () => clearTimeout(timeout)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [searchDraft])

    const fetchUsers = () => {
        setLoading(true)
        axiosInstance
            .get('/admin/users', {params: {search: search || undefined, offset, limit: pageSize}})
            .then((response) => {
                setUsers(response.data?.users || [])
                setTotalCount(response.data?.totalCount || 0)
            })
            .catch(() => {})
            .finally(() => setLoading(false))
    }

    useEffect(() => {
        if (!ready) {
            return
        }
        fetchUsers()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ready, offset, pageSize, search])

    const setDisabled = (user, disabled) => {
        const action = disabled ? 'disable' : 'enable'
        axiosInstance
            .post(`/admin/users/${user.id}/${action}`)
            .then(() => {
                toast.success(getMessage('ka', disabled ? 'adminUsersDisabled' : 'adminUsersEnabled'))
                fetchUsers()
            })
            .catch(() => {})
    }

    const handleToggleDisabled = (user) => {
        const disabling = !user.disabled
        showConfirmDialog({
            type: disabling ? 'warning' : 'info',
            title: getMessage('ka', disabling ? 'adminUsersDisableConfirmTitle' : 'adminUsersEnableConfirmTitle'),
            message: getMessage('ka', disabling ? 'adminUsersDisableConfirmMessage' : 'adminUsersEnableConfirmMessage', user.username),
            onConfirm: () => setDisabled(user, disabling),
        })
    }

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
            <Container maxWidth="lg">
                <Box sx={{marginBottom: '1rem', marginTop: '1rem'}}>
                    <TextField
                        size="small"
                        label={getMessage('ka', 'adminUsersSearchLabel')}
                        value={searchDraft}
                        onChange={(event) => setSearchDraft(event.target.value)}
                        sx={{minWidth: 280}}
                    />
                </Box>
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>{getMessage('ka', 'adminUsersUsernameColumn')}</TableCell>
                                <TableCell>{getMessage('ka', 'adminUsersFirstNameColumn')}</TableCell>
                                <TableCell>{getMessage('ka', 'adminUsersLastNameColumn')}</TableCell>
                                <TableCell>{getMessage('ka', 'adminUsersStatusColumn')}</TableCell>
                                <TableCell align="right">{getMessage('ka', 'adminUsersActionsColumn')}</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={5} align="center">
                                        <CircularProgress size={24} />
                                    </TableCell>
                                </TableRow>
                            ) : users.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={5} align="center">
                                        {getMessage('ka', 'adminUsersNoResults')}
                                    </TableCell>
                                </TableRow>
                            ) : (
                                users.map((user) => {
                                    const isAdmin = (user.role || '').includes('ADMIN')
                                    return (
                                        <TableRow key={user.id} hover>
                                            <TableCell>{user.username}</TableCell>
                                            <TableCell>{user.firstName}</TableCell>
                                            <TableCell>{user.lastName}</TableCell>
                                            <TableCell>
                                                <Chip
                                                    label={getMessage('ka', user.disabled ? 'adminUsersStatusDisabled' : 'adminUsersStatusActive')}
                                                    color={user.disabled ? 'error' : 'success'}
                                                    size="small"
                                                    sx={{marginRight: 1}}
                                                />
                                                {formatLastLogin(user.lastLogin)}
                                            </TableCell>
                                            <TableCell align="right">
                                                <Tooltip title={getMessage('ka', 'adminUsersMonitorComingSoon')}>
                                                    <span>
                                                        <IconButton size="small" disabled>
                                                            <VisibilityIcon fontSize="small" />
                                                        </IconButton>
                                                    </span>
                                                </Tooltip>
                                                {!isAdmin && (
                                                    <Tooltip title={getMessage('ka', user.disabled ? 'adminUsersEnable' : 'adminUsersDisable')}>
                                                        <IconButton
                                                            size="small"
                                                            color={user.disabled ? 'success' : 'error'}
                                                            onClick={() => handleToggleDisabled(user)}
                                                        >
                                                            {user.disabled ? <CheckCircleIcon fontSize="small" /> : <BlockIcon fontSize="small" />}
                                                        </IconButton>
                                                    </Tooltip>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    )
                                })
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
                <PaginationControls pagination={pagination} />
            </Container>
        </main>
    )
}
