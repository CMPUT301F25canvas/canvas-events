# Lottery Event System - Team canvas
This application allows entrants the chance to join different types of events through a lottery system. Rather than relying on luck or having to refresh for new events, users can join an event's waitlist and wait for a lottery system to see if they are able to join the event.


# Members: 
| **Name** | **CCID** | **GitHub Username** |
|----------|----------|---------------------|
| Lance Quinto | ciquinto | clanceiq |
| Vijay Polisetti | polisett | Vijayasaradhi-p |
| Emily Lan | zlan4 | zlan4 |
| Gui Carius | carius | gCarius |
| Ethan Kinch | epkinch | epkinch |
| Faraz Oghbaei | oghbaei | FarazOghbaei-ML |
farazoghbaei@Farazs-MacBook-Air functions % gcloud auth activate-service-account --key-file "/Users/farazoghbaei/Desktop/canvas-events-dev-firebase-adminsdk-fbsvc-66effbc35c.json"
gcloud config set project canvas-events-dev
gcloud iam service-accounts add-iam-policy-binding \
  canvas-events-dev@appspot.gserviceaccount.com \
  --member="serviceAccount:firebase-adminsdk-fbsvc@canvas-events-dev.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser" \
  --project=canvas-events-dev
Activated service account credentials for: [firebase-adminsdk-fbsvc@canvas-events-dev.iam.gserviceaccount.com]
Updated property [core/project].
ERROR: (gcloud.iam.service-accounts.add-iam-policy-binding) PERMISSION_DENIED: Permission 'iam.serviceAccounts.getIamPolicy' denied on resource (or it may not exist). This command is authenticated as firebase-adminsdk-fbsvc@canvas-events-dev.iam.gserviceaccount.com which is the active account specified by the [core/account] property.
- '@type': type.googleapis.com/google.rpc.ErrorInfo
  domain: iam.googleapis.com
  metadata:
    permission: iam.serviceAccounts.getIamPolicy
  reason: IAM_PERMISSION_DENIED
farazoghbaei@Farazs-MacBook-Air functions %