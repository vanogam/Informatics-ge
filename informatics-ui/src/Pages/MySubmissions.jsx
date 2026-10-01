import {useState} from 'react'
import {useParams} from 'react-router-dom'
import ContestNavigationBar from "../Components/ContestNavigationBar";
import SubmissionsList from "../Components/SubmissionsList";
import SubmissionFilters, {EMPTY_FILTERS} from '../Components/SubmissionFilters'

export default function MySubmissions() {
    const {contest_id} = useParams()
    const [filters, setFilters] = useState(EMPTY_FILTERS)

    const getEndpoint = () => `/contest/${contest_id}/submissions`

    return (
        <>
            <ContestNavigationBar />
            <SubmissionFilters
                value={filters}
                onChange={setFilters}
                fixedContestId={contest_id}
            />
            <SubmissionsList
                getEndpoint={getEndpoint}
                title="ჩემი მცდელობები"
                filters={filters}
            />
        </>
    )
}
