import {useParams} from 'react-router-dom'
import {
    Container,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Button,
    Modal,
    Box,
    TableContainer,
    Paper,
    Stack
} from '@mui/material'
import {NavLink} from "react-router-dom";
import {useState, useEffect, useContext} from 'react';
import {AuthContext} from '../store/authentication'
import ContestRegisterPopUp from '../Components/ContestRegisterPopUp'
import {AxiosContext} from '../utils/axiosInstance'
import ContestNavigationBar from '../Components/ContestNavigationBar'
import TaskTags from '../Components/TaskTags'

function handleContestResponse(response, setProblems) {
    var curTasks = []
    const tasks = response.data.tasks
    // Sort tasks by order
    const sortedTasks = [...tasks].sort((a, b) => {
        const orderA = a.task?.order || 0
        const orderB = b.task?.order || 0
        return orderA - orderB
    })
    
    for (const task of sortedTasks) {
        const taskId = task.task.id
        const taskName = task.task.title
        const taskOrder = task.task.order || 0
        const score = task.score
        const taskItem = {
            id: taskId,
            name: taskName,
            order: taskOrder,
            category: taskOrder || 0,
            score: score,
            tags: task.tags || []
        }
        curTasks.push(taskItem)
    }
    setProblems(curTasks)
}

export default function Contest() {
    const [popUp, setPopUp] = useState(false)
    const authContext = useContext(AuthContext)
    const isLoggedIn = authContext.isLoggedIn
    const role = authContext.role
    const {contest_id} = useParams()
    const [problems, setProblems] = useState([])
    const [registered, setIsRegistered] = useState(false)
    const [allTags, setAllTags] = useState([])
    const axiosInstance = useContext(AxiosContext)
    const isStaff = (role || '').includes('ADMIN') || (role || '').includes('TEACHER')

    useEffect(() => {
        axiosInstance
            .get(`/contest/${contest_id}/tasks`, {
                params: {
                    offset: 0,
                    limit: 20
                }
            })
            .then((response) => {
                handleContestResponse(response, setProblems)
                axiosInstance
                    .get(`/contest/${contest_id}/is-registered`)
                    .then((response) => {

                        if (response.data.registered) {
                            setIsRegistered(true)
                        }
                    })
            })
            .catch((error) => console.log(error))
    }, [])

    useEffect(() => {
        if (!isStaff) {
            return
        }
        axiosInstance.get('/tags')
            .then((response) => setAllTags(response.data.tags || []))
            .catch(_ => {})
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isStaff])

    const setRowTags = (taskId) => (newTags) => {
        setProblems((prev) => prev.map((p) => (p.id === taskId ? {...p, tags: newTags} : p)))
    }

    return (
        <main>
            <ContestNavigationBar />
            <Modal open={popUp} onClose={() => setPopUp(false)}>
                <Box
                    sx={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                        width: '350px',
                        bgcolor: 'white',
                        border: `2px solid ;`,
                        borderRadius: '0.5rem',
                        boxShadow: 24,
                        p: 4,
                    }}
                >
                    <ContestRegisterPopUp contestId={contest_id}/>
                </Box>
            </Modal>
            <Typography variant="h6"
                        fontWeight="bold"
                        mt="1rem"
                        align="center"
                        sx={{color: '#452c54', fontWeight: 'bold'}}>
                კონტესტის ამოცანები
            </Typography>
            <Typography
                paragraph
                align="center"
                pt="0.5rem"
                pb="1rem"
                borderBottom="2px dashed #aaa"
            >

            </Typography>
            <Container maxWidth="lg">
                <TableContainer component={Paper} sx={{marginInline: 'auto'}}>
                    <Table sx={{marginX: 'auto'}}>
                        <TableHead>
                            <TableRow>

                                <TableCell sx={{width: '10%'}}>ნომერი</TableCell>
                                <TableCell sx={{width: '80%'}}>სახელი</TableCell>
                                {(isLoggedIn && !(role === 'ADMIN') && registered === true) &&
                                    (
                                        <TableCell sx={{width: '10%'}}>ქულა</TableCell>
                                    )
                                }
                                {(isLoggedIn && !(role === 'ADMIN') && registered === false) && (
                                    <TableCell>
                                        <Button
                                            className="items"
                                            variant="contained"
                                            color="success"
                                            sx={{background: '#3c324e'}}
                                            onClick={() => {
                                                setPopUp(true)
                                            }}

                                        >
                                            რეგისტრაცია
                                        </Button>
                                    </TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {problems.map((problem) => (
                                <TableRow
                                    onClick={
                                        () => {
                                            window.location.href = `/contest/${contest_id}/problem/${problem.id}`;
                                        }
                                    }
                                    key={problem.name}
                                    sx={{
                                        '&:last-child td, &:last-child th': { border: 0 },
                                        cursor: 'pointer',
                                    }}
                                >

                                    <TableCell component="th" scope="row">
                                        {problem.order || problem.category}
                                    </TableCell>

                                    <TableCell>
                                        <Stack direction="row" alignItems="center" spacing={1} flexWrap="wrap" sx={{width: '100%'}}>
                                            <NavLink style={{color: 'black', textDecorationLine: 'none'}}
                                                     to={`${problem.id}`}
                                                     exact={"true"}>{problem.name} </NavLink>
                                            <TaskTags
                                                taskId={problem.id}
                                                tags={problem.tags}
                                                setTags={setRowTags(problem.id)}
                                                editable={isStaff}
                                                allTags={allTags}
                                            />
                                        </Stack>
                                    </TableCell>
                                    {(isLoggedIn && (role === 'ADMIN' || registered === true)) &&
                                        (
                                            <TableCell>{problem.score}</TableCell>
                                        )
                                    }
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Container>
        </main>
    )

};