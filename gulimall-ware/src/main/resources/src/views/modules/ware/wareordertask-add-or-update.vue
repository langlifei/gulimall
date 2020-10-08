<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="order_id" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="order_id"></el-input>
    </el-form-item>
    <el-form-item label="order_sn" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="order_sn"></el-input>
    </el-form-item>
    <el-form-item label="" prop="consignee">
      <el-input v-model="dataForm.consignee" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="consigneeTel">
      <el-input v-model="dataForm.consigneeTel" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="deliveryAddress">
      <el-input v-model="dataForm.deliveryAddress" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="orderComment">
      <el-input v-model="dataForm.orderComment" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="paymentWay">
      <el-input v-model="dataForm.paymentWay" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="taskStatus">
      <el-input v-model="dataForm.taskStatus" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="orderBody">
      <el-input v-model="dataForm.orderBody" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="trackingNo">
      <el-input v-model="dataForm.trackingNo" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="create_time" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="create_time"></el-input>
    </el-form-item>
    <el-form-item label="" prop="wareId">
      <el-input v-model="dataForm.wareId" placeholder=""></el-input>
    </el-form-item>
    <el-form-item label="" prop="taskComment">
      <el-input v-model="dataForm.taskComment" placeholder=""></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          orderId: '',
          orderSn: '',
          consignee: '',
          consigneeTel: '',
          deliveryAddress: '',
          orderComment: '',
          paymentWay: '',
          taskStatus: '',
          orderBody: '',
          trackingNo: '',
          createTime: '',
          wareId: '',
          taskComment: ''
        },
        dataRule: {
          orderId: [
            { required: true, message: 'order_id不能为空', trigger: 'blur' }
          ],
          orderSn: [
            { required: true, message: 'order_sn不能为空', trigger: 'blur' }
          ],
          consignee: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          consigneeTel: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          deliveryAddress: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          orderComment: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          paymentWay: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          taskStatus: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          orderBody: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          trackingNo: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: 'create_time不能为空', trigger: 'blur' }
          ],
          wareId: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          taskComment: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/ware/wareordertask/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.orderId = data.wareOrderTask.orderId
                this.dataForm.orderSn = data.wareOrderTask.orderSn
                this.dataForm.consignee = data.wareOrderTask.consignee
                this.dataForm.consigneeTel = data.wareOrderTask.consigneeTel
                this.dataForm.deliveryAddress = data.wareOrderTask.deliveryAddress
                this.dataForm.orderComment = data.wareOrderTask.orderComment
                this.dataForm.paymentWay = data.wareOrderTask.paymentWay
                this.dataForm.taskStatus = data.wareOrderTask.taskStatus
                this.dataForm.orderBody = data.wareOrderTask.orderBody
                this.dataForm.trackingNo = data.wareOrderTask.trackingNo
                this.dataForm.createTime = data.wareOrderTask.createTime
                this.dataForm.wareId = data.wareOrderTask.wareId
                this.dataForm.taskComment = data.wareOrderTask.taskComment
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/ware/wareordertask/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'orderId': this.dataForm.orderId,
                'orderSn': this.dataForm.orderSn,
                'consignee': this.dataForm.consignee,
                'consigneeTel': this.dataForm.consigneeTel,
                'deliveryAddress': this.dataForm.deliveryAddress,
                'orderComment': this.dataForm.orderComment,
                'paymentWay': this.dataForm.paymentWay,
                'taskStatus': this.dataForm.taskStatus,
                'orderBody': this.dataForm.orderBody,
                'trackingNo': this.dataForm.trackingNo,
                'createTime': this.dataForm.createTime,
                'wareId': this.dataForm.wareId,
                'taskComment': this.dataForm.taskComment
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
