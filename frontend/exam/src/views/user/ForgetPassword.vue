<template>
  <div class="main">
    <a-form
      id="formLogin"
      class="user-layout-login"
      ref="formLogin"
      :form="form"
      @submit="handleSubmit"
    >
      <a-tabs
        :activeKey="customActiveKey"
        :tabBarStyle="{ textAlign: 'center', borderBottom: 'unset' }"
        @change="handleTabClick"
      >
        <a-tab-pane key="tab1" tab="在线考试系统重置密码">
          <a-form-item>
            <a-input
              size="large"
              type="text"
              placeholder="请输入邮箱"
              v-decorator="[
                'userEmail',
                {rules: [{ required: true, message: '请输入邮箱' }], validateTrigger: 'change'}
              ]"
            >
              <a-icon slot="prefix" type="user" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>

			<a-form-item style=" margin-bottom:5px ">
		  <template>
			  <div>请选择验证问题</div>
		    <div>
		      <a-select style="width: 300px" @change="handleChange"   v-decorator="['valiQuestion', { validateTrigger: ['change', 'blur']}]">
		        <a-select-option value="请选择验证问题" disabled>
		          请选择验证问题
		        </a-select-option>              
		        <a-select-option value="你父亲的名字是什么？">
		          你父亲的名字是什么？
		        </a-select-option>
		        <a-select-option value="你母亲的名字是什么？">
		          你母亲的名字是什么？
		        </a-select-option>
		        <a-select-option value="对你影响最深的人是谁？">
		          对你影响最深的人是谁
		        </a-select-option>
		        <a-select-option value="你小学的名字是什么？">
		          你小学的名字是什么？
		        </a-select-option>
		      </a-select>
		    </div>
		  </template>
		</a-form-item>
		
		<a-form-item>
		      <a-input
		        size="large"
		        type="text"
		        placeholder="请输入你的回答"
		        v-decorator="['valiQuestionAnswer', {rules: [{ required: true, message: '请输入你的回答' }], validateTrigger: ['change', 'blur']}]"
		      ></a-input>
		</a-form-item>      
        </a-tab-pane>
      </a-tabs>

		<a-form-item style="margin-top:24px">
			<a-button
			  size="large"
			  type="primary"
			  htmlType="submit"
			  class="login-button"
			  :loading="state.loginBtn"
			  :disabled="state.loginBtn"
			  @click.stop.prevent="handleSubmit"
			>确定
			</a-button>
		</a-form-item>
    </a-form>

    <two-step-captcha
      v-if="requiredTwoStepCaptcha"
      :visible="stepCaptchaVisible"
      @success="stepCaptchaSuccess"
      @cancel="stepCaptchaCancel"
    ></two-step-captcha>
  </div>
</template>

<script>
import TwoStepCaptcha from '../../components/tools/TwoStepCaptcha'
import { mapActions } from 'vuex'
import { timeFix } from '../../utils/util'
import { getSmsCaptcha, get2step } from '../../api/login'
import { forgetPassword } from '../../api/user'

export default {
  components: {
    TwoStepCaptcha
  },
  data () {
    return {
      customActiveKey: 'tab1',
      loginBtn: false,
      // login type: 0 email, 1 username, 2 telephone
      loginType: 0,
      requiredTwoStepCaptcha: false,
      stepCaptchaVisible: false,
      form: this.$form.createForm(this),
      state: {
        time: 60,
        loginBtn: false,
        // login type: 0 email, 1 username, 2 telephone
        loginType: 0,
        smsSendBtn: false
      }
    }
  },
  created () {
    get2step({})
      .then(res => {
        this.requiredTwoStepCaptcha = res.result.stepCode
      })
      .catch(() => {
        this.requiredTwoStepCaptcha = false
      })
    // this.requiredTwoStepCaptcha = true
  },
  methods: {
    ...mapActions(['Login', 'Logout']), // 这个是从Vuex中直接继承过来，从而可以当本地方法用，见store/modules/user.js
    // handler
    handleTabClick (key) {
      this.customActiveKey = key
      // this.form.resetFields()
    },
    handleSubmit (e) {
      const { form: { validateFields }, $router, $message } = this
      validateFields({ force: true }, (err, values) => {
        if (!err) {
          // 在这里进行Rest请求，params参数如右：{email: "1648266192@qq.com", password: "123456", password2: "123456", mobile: "17601324488", captcha: "69076"}
          forgetPassword(values).then(res => {
            // 成功就跳转到结果页面
            console.log(res)
            $router.push({ name: 'resetPassword', params: { ...values } })
          }).catch(err => {
            // 失败就弹出警告消息
            $message.error(`load user err: ${err.message}`)
          })
        }
      })
    },
    getCaptcha (e) {
      e.preventDefault()
      const { form: { validateFields }, state } = this

      validateFields(['mobile'], { force: true }, (err, values) => {
        if (!err) {
          state.smsSendBtn = true

          const interval = window.setInterval(() => {
            if (state.time-- <= 0) {
              state.time = 60
              state.smsSendBtn = false
              window.clearInterval(interval)
            }
          }, 1000)

          const hide = this.$message.loading('验证码发送中..', 0)
          getSmsCaptcha({ mobile: values.mobile }).then(res => {
            setTimeout(hide, 2500)
            this.$notification['success']({
              message: '提示',
              description: '验证码获取成功，您的验证码为：' + res.result.captcha,
              duration: 8
            })
          }).catch(err => {
            setTimeout(hide, 1)
            clearInterval(interval)
            state.time = 60
            state.smsSendBtn = false
            this.requestFailed(err)
          })
        }
      })
    },
    stepCaptchaSuccess () {
      this.loginSuccess()
    },
    stepCaptchaCancel () {
      this.Logout().then(() => {
        this.loginBtn = false
        this.stepCaptchaVisible = false
      })
    },
    loginSuccess (res) {
      console.log(res)
      this.$router.push({ name: 'dashboard' })
      // 延迟 1 秒显示欢迎信息
      setTimeout(() => {
        this.$notification.success({
          message: '欢迎',
          description: `${timeFix()}，欢迎回来`
        })
      }, 1000)
    },
    requestFailed (err) {
      this.$notification['error']({
        message: '错误',
        description: ((err.response || {}).data || {}).message || '用户名或密码错误',
        duration: 4
      })
    }
  }
}
</script>

<style lang="less" scoped>
  .user-layout-login {
    label {
      font-size: 14px;
    }

    .getCaptcha {
      display: block;
      width: 100%;
      height: 40px;
    }

    .forge-password {
      font-size: 14px;
    }

    button.login-button {
      padding: 0 15px;
      font-size: 16px;
      height: 40px;
      width: 100%;
    }

    .user-login-other {
      text-align: left;
      margin-top: 24px;
      line-height: 22px;

      .item-icon { 
        font-size: 24px;
        color: rgba(0, 0, 0, 0.2);
        margin-left: 16px;
        vertical-align: middle;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #1890ff;
        }
      }

      .register {
        float: right;
      }
    }
  }
</style>
