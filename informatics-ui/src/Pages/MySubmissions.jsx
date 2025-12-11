import {useParams} from 'react-router-dom'
import ContestNavigationBar from "../Components/ContestNavigationBar";
import SubmissionsList from "../Components/SubmissionsList";

export default function MySubmissions() {
    const {contest_id} = useParams()
    
    const getEndpoint = () => `/contest/${contest_id}/submissions`
    
    return (
        <>
            <ContestNavigationBar />
            <SubmissionsList 
                getEndpoint={getEndpoint}
                title="ჩემი მცდელობები"
            />
        </>
    )
}
