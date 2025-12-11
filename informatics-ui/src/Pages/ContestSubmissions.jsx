import {useParams} from 'react-router-dom'
import ContestNavigationBar from '../Components/ContestNavigationBar'
import SubmissionsList from '../Components/SubmissionsList'

export default function ContestSubmissions() {
    const {contest_id} = useParams()
    
    const getEndpoint = () => `/contest/${contest_id}/status`
    
    return (
        <>
            <ContestNavigationBar />
            <SubmissionsList 
                getEndpoint={getEndpoint}
                title="მცდელობები"
            />
        </>
    )
}
    