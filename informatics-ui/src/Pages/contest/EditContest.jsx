import getMessage from "../../Components/lang";
import {useContext, useEffect, useState} from 'react';
import {AxiosContext} from "../../utils/axiosInstance";
import {NavLink, useParams} from 'react-router-dom';
import dayjs from "dayjs";
import {LocalizationProvider} from "@mui/x-date-pickers/LocalizationProvider";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {Box, Button, Container, Paper, Stack, Typography} from "@mui/material";
import ContestForm from "./contestForm";

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
    })


    useEffect(() => {
        axiosInstance.get(`/contest/${params.contest_id}`).then((response) => {
            const contest = response.data
            setName(contest.name)
            setTasks(contest.tasks)
            setContestData((prevData) => ({
                ...prevData,
                contestName: contest.name,
                startDate: dayjs(contest.startDate),
                duration: contest.durationInSeconds / 60,
                archive: contest.upsolving,
                autoArchive: contest.upsolvingAfterFinish,
            }))
            setInitialName(contest.name)
        })
    }, [axiosInstance, params.contest_id])

    const handleAddContest = () => {
        const params = {
            name: contestData.contestName,
            startDate: contestData.startDate?.format('DD/MM/YYYY HH:mm'),
            durationInSeconds: contestData.duration * 60,
            roomId: '1',
            contestId: parseInt(params.contest_id),
            upsolving: contestData.archive,
            upsolvingAfterFinish: contestData.autoArchive,
        }
        params['durationInSeconds'] = params['durationInSeconds'].toString()
        console.log(params)
        axiosInstance.post('/contest', params).then(() => {
            setSaved(true)
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
                                sx={{padding: '1rem', marginBottom: '0.5rem'}}
                                key={task}
                            >
                                <Typography>
												<span style={{fontWeight: 700}}>
													<Box sx={{
                                                        display: 'flex',
                                                        justifyContent: 'space-between',
                                                        width: '100%'
                                                    }}>
														<Box>
															#{index + 1} : {task.title}
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
                                    {/*{tasks}*/}
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
                <Button
                    sx={{background: '#3c324e'}} variant='contained' size='large'
                    component={NavLink}
                    to='/contests'
                >
                    დასრულება
                </Button>
            </Container>
        </LocalizationProvider>
    )
}