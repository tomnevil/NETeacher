import Card from '../components/ui/Card'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { listPlans, enroll, myMembership } from '../api/ops'
import type { MembershipPlan } from '../api/types'

export default function Membership() {
  const qc = useQueryClient()
  const { data: plans } = useQuery({
    queryKey: ['plans'],
    queryFn: () => listPlans().then((r) => r.data.data)
  })
  const { data: mine } = useQuery({
    queryKey: ['my-membership'],
    queryFn: () => myMembership().then((r) => r.data.data)
  })

  const mutation = useMutation({
    mutationFn: (tier: string) => enroll(tier),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['my-membership'] })
  })

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">会员中心</h1>

      {mine && (
        <Card>
          <div className="text-brand-800 font-semibold mb-1">当前会员</div>
          <div className="text-sm text-gray-600">
            {mine.plan} {mine.expireAt ? `· 到期 ${mine.expireAt}` : '· 长期有效'}
          </div>
        </Card>
      )}

      <div className="grid sm:grid-cols-2 gap-4">
        {plans?.map((p: MembershipPlan) => (
          <Card key={p.id}>
            <div className="flex items-center justify-between">
              <div className="font-bold text-lg text-brand-800">{p.name}</div>
              <div className="text-accent-500 font-semibold">
                {p.priceMonths === 0 ? '免费' : `¥${(p.priceMonths / 100).toFixed(2)}/月`}
              </div>
            </div>
            <div className="text-sm text-gray-500 mt-2 whitespace-pre-wrap">{p.benefits}</div>
            <button
              disabled={mutation.isPending || mine?.plan === p.tier}
              onClick={() => mutation.mutate(p.tier)}
              className="mt-3 w-full bg-brand-600 hover:bg-brand-700 text-white rounded-2xl py-2 disabled:opacity-50"
            >
              {mine?.plan === p.tier ? '当前套餐' : '立即开通'}
            </button>
          </Card>
        ))}
      </div>
    </div>
  )
}
