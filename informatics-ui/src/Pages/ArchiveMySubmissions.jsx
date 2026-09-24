import ContestNavigationBar from "../Components/ContestNavigationBar";
import SubmissionsList from "../Components/SubmissionsList";

const GLOBAL_ROOM_ID = 1

export default function ArchiveMySubmissions() {
    const getEndpoint = () => `/room/${GLOBAL_ROOM_ID}/submissions`

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
