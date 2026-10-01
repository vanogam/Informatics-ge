import {useState} from 'react'
import ContestNavigationBar from "../Components/ContestNavigationBar";
import SubmissionsList from "../Components/SubmissionsList";
import SubmissionFilters, {EMPTY_FILTERS} from '../Components/SubmissionFilters'

const GLOBAL_ROOM_ID = 1

export default function ArchiveMySubmissions() {
    const [filters, setFilters] = useState(EMPTY_FILTERS)

    const getEndpoint = () => `/room/${GLOBAL_ROOM_ID}/submissions`

    return (
        <>
            <ContestNavigationBar />
            <SubmissionFilters
                value={filters}
                onChange={setFilters}
                showContest
                contestsRoomId={GLOBAL_ROOM_ID}
            />
            <SubmissionsList
                getEndpoint={getEndpoint}
                title="ჩემი მცდელობები"
                filters={filters}
            />
        </>
    )
}
