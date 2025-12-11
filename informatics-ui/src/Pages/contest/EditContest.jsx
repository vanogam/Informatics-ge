import getMessage from "../../Components/lang";
import {useContext, useEffect, useState} from 'react';
import {AxiosContext} from "../../utils/axiosInstance";
import {NavLink, useParams} from 'react-router-dom';
import dayjs from "dayjs";
import {LocalizationProvider} from "@mui/x-date-pickers/LocalizationProvider";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {Box, Button, Container, Paper, Stack, Typography} from "@mui/material";
import ContestForm from "./contestForm";
import {toast} from "react-toastify";

export default function EditContest() {
    const axiosInstance = useContext(AxiosContext)
    const params = useParams()
    const [name, setName] = useState(null)
    const [initialName, setInitialName] = useState('')
    const [saved, setSaved] = useState(null)
    const [tasks, setTasks] = useState([])
    const [contestData, setContestData] = useState({
        contestName: ' ',
        startDate: null,
        duration: 0,
        archive: false,
        autoArchive: false,
        scoringType: 'BEST_SUBMISSION',
    })


    useEffect(() => {
        axiosInstance.get(`/contest/${params.contest_id}`).then((response) => {
            const contest = response.data
            setName(contest.name)
            const sortedTasks = [...(contest.tasks || [])].sort((a, b) => {
                const orderA = a.order || 0
                const orderB = b.order || 0
                return orderA - orderB
            })
            setTasks(sortedTasks)
            setContestData((prevData) => ({
                ...prevData,
                contestName: contest.name,
                startDate: dayjs(contest.startDate),
                duration: dayjs(contest.endDate).diff(dayjs(contest.startDate), 'minute'),
                archive: contest.upsolving,
                autoArchive: contest.upsolvingAfterFinish,
                scoringType: contest.scoringType || 'BEST_SUBMISSION',
            }))
            setInitialName(contest.name)
        })
    }, [axiosInstance, params.contest_id])

    const [draggedIndex, setDraggedIndex] = useState(null)

    const handleDragStart = (index) => {
        setDraggedIndex(index)
    }

    const handleDragOver = (e, index) => {
        e.preventDefault()
    }

    const handleDrop = (e, dropIndex) => {
        e.preventDefault()
        if (draggedIndex === null || draggedIndex === dropIndex) {
            setDraggedIndex(null)
            return
        }

        const newTasks = [...tasks]
        const draggedTask = newTasks[draggedIndex]
        newTasks.splice(draggedIndex, 1)
        newTasks.splice(dropIndex, 0, draggedTask)
        setTasks(newTasks)
        setDraggedIndex(null)

        // Update order on backend
        const taskIds = newTasks.map(task => task.id)
        axiosInstance.put(`/contest/${params.contest_id}/tasks/order`, { taskIds })
            .then(() => {
                toast.success(getMessage('ka', 'saved') || 'Order updated')
            })
            .catch((error) => {
                console.error('Error updating task order:', error)
                toast.error('Failed to update task order')
                // Reload tasks on error
                axiosInstance.get(`/contest/${params.contest_id}`).then((response) => {
                    const contest = response.data
                    const sortedTasks = [...(contest.tasks || [])].sort((a, b) => {
                        const orderA = a.order || 0
                        const orderB = b.order || 0
                        return orderA - orderB
                    })
                    setTasks(sortedTasks)
                })
            })
    }

    const handleAddContest = () => {
        const requestParams = {
            name: contestData.contestName,
            startDate: contestData.startDate?.format('DD/MM/YYYY HH:mm'),
            durationInSeconds: contestData.startDate && contestData.duration
                ? contestData.duration * 60
                : null,
            roomId: 1,
            upsolving: contestData.archive,
            upsolvingAfterFinish: contestData.autoArchive,
            scoringType: contestData.scoringType,
        }
        axiosInstance.put(`/contest/${params.contest_id}`, requestParams).then(() => {
            setSaved(true)
            toast.success(getMessage('ka', 'saved'))
        })
    }

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Container maxWidth='xs'>
                <Stack gap="1rem" marginTop="2rem">
                    <Paper elevation={4} sx={{padding: '1rem'}}>

                        <Typography variant="h5" align="center" pb="1rem">
                            {getMessage('ka', 'editContest', initialName)}
                        </Typography>
                        <ContestForm contestData={contestData}
                                     setContestData={setContestData}
                                     handleSubmit={handleAddContest}
                                     buttonText={getMessage('ka', 'edit')}/>
                    </Paper>
                </Stack>
                <Paper
                    elevation={4}
                    sx={{padding: '1rem', marginBottom: '0.5rem'}}
                >
                    <Typography
                        textAlign='center'
                        variant='h6'
                        marginBottom='0.5rem'
                    >
                        ამოცანები
                    </Typography>
                    <Stack>
                        {tasks?.map((task, index) => (
                            <Paper
                                elevation={4}
                                sx={{
                                    padding: '1rem',
                                    marginBottom: '0.5rem',
                                    cursor: 'move',
                                    opacity: draggedIndex === index ? 0.5 : 1,
                                    '&:hover': {
                                        backgroundColor: '#f5f5f5'
                                    }
                                }}
                                key={task.id}
                                draggable
                                onDragStart={() => handleDragStart(index)}
                                onDragOver={(e) => handleDragOver(e, index)}
                                onDrop={(e) => handleDrop(e, index)}
                            >
                                <Typography>
												<span style={{fontWeight: 700}}>
													<Box sx={{
                                                        display: 'flex',
                                                        justifyContent: 'space-between',
                                                        width: '100%'
                                                    }}>
														<Box>
															#{task.order || index + 1} : {task.title}
														</Box>
														<Box sx={{display: 'flex', justifyContent: 'flex-end'}}>
															<Button component={NavLink}
                                                                    variant='contained'
                                                                    sx={{background: '#3c324e'}}
                                                                    to={`/task/${task.id}`}
                                                                    target='_blank'>
																{getMessage('ka', 'edit')}
															</Button>
														</Box>
													</Box>
												</span>
                                </Typography>
                            </Paper>
                        ))}
                        <Paper elevation={4} sx={{padding: '1rem'}}>
                            <Button
                                fullWidth
                                component={NavLink}
                                variant='contained'
                                sx={{background: '#3c324e'}}
                                to={`/contest/${params.contest_id}/task/add`}
                                target='_blank'
                            >
                                {getMessage('ka', 'addProblem')}
                            </Button>
                        </Paper>
                    </Stack>
                </Paper>
            </Container>
        </LocalizationProvider>
    )
}