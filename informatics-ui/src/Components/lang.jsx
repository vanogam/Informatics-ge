

const langDictionary = {
	'ka' : {
		// ავტორიზაცია და რეგისტრაცია
		'registerSuccess': 'რეგისტრაცია წარმატებით დასრულდა',
		'loginSuccess': 'ავტორიზაცია წარმატებულია',
		'incorrectCredentials': 'არასწორი სახელი ან პაროლი',
		'ERROR_CODE_incorrectCredentials': 'არასწორი სახელი ან პაროლი',
		'ERROR_CODE_loggedInWithDifferentUser': 'სხვა მომხმარებელი უკვე ავტორიზებულია',

		// კონტესტები
		'name': 'სახელი',
		'duration': 'ხანგრძლივობა',
		'hourShort': 'სთ',
		'minuteShort': 'წთ',
		'DATEFORMAT_Minutes': 'წუთი',
		'DATEFORMAT_Hours': 'საათი',
		'scoringType': 'გაშვების პრიორიტეტი',
		'SCORING_TYPE_BEST_SUBMISSION': 'საუკეთესო გაშვება',
		'SCORING_TYPE_LAST_SUBMISSION': 'ბოლო გაშვება',
		'upsolvingAfterFinished': 'ამოცანების აფსოლვინგში დამატება შეჯიბრების დასრულების შემდეგ',
		'startDate': 'დაწყების დრო',

		'unexpectedException': 'გაუთვალისწინებელი შეცდომა',
		'insufficientPrivileges': 'ამ ქმედების განხორციელების უფლება არ გაქვთ!',
		'pleaseLogin': 'გთხოვთ, გაიაროთ ავტორიზაცია',
		'missingRequiredFields': 'გთხოვთ, შეავსოთ აუცილებელი ველები',
		'startDateAndDurationError': 'დაწყების თარიღი და ხანგრძლივობიდან ერთ-ერთი არ შეიძლება იყოს ცარიელი(დატოვეთ ორივე ცარიელი საარქივო კონტესტის შექმნის შემთხვევაში)',

		'addProblem': 'ამოცანის დამატება',
		'addContest': 'დაამატე შეჯიბრება',
		'editContest': '{0}-ის რედაქტირება',
		'contestAdded': 'შეჯიბრება წარმატებით დაემატა',

		'title': 'სათაური',
		'kaStatement': 'პირობა',
		'taskCode': 'ამოცანის უნიკალური კოდი',
		'taskType': 'ამოცანის ტიპი',
		'taskScoreType': 'ქულების დაწერის წესის ტიპი',
		'taskSuccessfullyAdded': 'ამოცანა წარმატებით დაემატა',
		'taskSuccessfullyEdited': 'ამოცანა წარმატებით განახლდა',
		'testcases': 'ტესტები',
		'addSingleTestcase': 'დაამატე ტესტი',
		'addMultipleTestcases': 'დაამატე რამდენიმე ტესტი',
		'uploadedFile': 'ფაილი',
		'uploadInputFile': 'შემავალი ფაილი',
		'uploadOutputFile': 'გამომავალი ფაილი',
		'uploadArchive': 'ატვირთე არქივი',
		'addedTestcase': 'დაემატა ტესტი',
		'addedTestcases': 'დაემატა ტესტები',
		'failedToAddTestcase': 'ვერ დაემატა',
		'failedToAddTestcases': 'ვერ დაემატა',
		'deleteSelected': 'მონიშნულის წაშლა',
		'downloadTestcases': 'ტესტების ჩამოტვირთვა',
		'testcaseDeleted': 'ტესტი წაშლილია',
		'add': 'დამატება',
		'save': 'შენახვა',

		'SUBMISSION_STATUS_IN_QUEUE': 'რიგშია...',
		'SUBMISSION_STATUS_RUNNING': 'გაშვებულია ტესტზე #{0}...',
		'SUBMISSION_STATUS_COMPILING': 'კომპილაცია...',
		'SUBMISSION_STATUS_SYSTEM_ERROR': 'სისტემური შეცდომა (დაუკავშირდით ადმინისტრატორს)',
		'SUBMISSION_STATUS_COMPILATION_ERROR': 'კომპილაციის შეცდომა',
		'SUBMISSION_STATUS_TIME_LIMIT_EXCEEDED': 'დროის ლიმიტი გადაჭარბებულია',
		'SUBMISSION_STATUS_MEMORY_LIMIT_EXCEEDED': 'მეხსიერების ლიმიტი გადაჭარბებულია',
		'SUBMISSION_STATUS_RUNTIME_ERROR': 'გაშვების შეცდომა',
		'SUBMISSION_STATUS_WRONG_ANSWER': 'არასწორი პასუხი',
		'SUBMISSION_STATUS_FAILED': 'წარუმატებელი გაშვება',
		'SUBMISSION_STATUS_PARTIAL': 'ნაწილობრივი',
		'SUBMISSION_STATUS_CORRECT': 'სწორია',

		'taskScoreParameter': 'ქულების დაწერის წესი',
		'timeLimitMillis': 'დროის ლიმიტი (მწ)',
		'memoryLimitMB': 'მეხსიერების ლიმიტი (MB)',
		'inputTemplate': 'შემავალი ფაილის სახელის შაბლონი',
		'outputTemplate': 'გამომავალი ფაილის სახელის შაბლონი',

		'TASK_SCORE_TYPE_SUM': 'ტესტების ჯამი',
		'TASK_SCORE_TYPE_GROUP_MIN': 'ტესტთა ჯგუფების ჯამი',

		'TASK_TYPE_BATCH': 'სტანდარტული',
		'edit': 'რედაქტირება',

		'tests': 'ტესტები',
		'inputContent': 'შემომავალი მონაცემები',
		'outputContent': 'გამომავალი მონაცემები',
		'correctOutput': 'სწორი პასუხი',
		'compilerMessage': 'ქომფაილერის შეტყობინება',

		'editor': 'რედაქტორი',
		'statement': 'პირობა',
		'markdownPlaceholder': 'აკრიფეთ ტექსტი...',
	}
}

const getMessage = (lang, message, ...args) => {
	if (!langDictionary[lang][message]) {
		return message;
	}
	let localizedMessage = langDictionary[lang][message];
	args.forEach((arg, index) => {
		localizedMessage = localizedMessage.replace(`{${index}}`, arg);
	});
	return localizedMessage;
};
export default getMessage;