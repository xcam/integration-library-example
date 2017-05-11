package ru.evotor.integrations.integrationlibraryexample

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.SimpleCursorAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ListView
import android.widget.Toast
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.OnActivityResult
import ru.evotor.framework.inventory.InventoryApi
import ru.evotor.framework.inventory.ProductItem
import ru.evotor.integrations.ReceiptsApi

@EActivity
open class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    lateinit var mAdapter: SimpleCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        val listView = findViewById(R.id.list) as ListView

        val toViews = intArrayOf(android.R.id.text1)
        mAdapter = SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null,
                arrayOf(ReceiptsApi.Positions.ROW_NAME), toViews, 0)
        listView.adapter = mAdapter

        supportLoaderManager.initLoader<Cursor>(0, Bundle(), this)
    }

    @Click(R.id.fab)
    fun onAddClick() {
        val intent = Intent("evotor.intent.action.commodity.SELECT")
        startActivityForResult(intent, REQUEST_CODE_ADD_COMMODITY)
    }

    @Click(R.id.send)
    fun onSendClick() {
        val intent = Intent("evotor.intent.action.payment.SELL")
        startActivityForResult(intent, REQUEST_CODE_PRINT)
    }

    @OnActivityResult(REQUEST_CODE_ADD_COMMODITY)
    fun onResult(resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            val uuid = data.extras.getString("commodityUuid")
            addProductByUuid(uuid)
        }
    }

    @Background
    open fun addProductByUuid(uuid: String) {
        val product = InventoryApi.getProductByUuid(this, uuid) ?: return

        if (product !is ProductItem.Product) {
            Toast.makeText(this, "Выбран не товар!", Toast.LENGTH_LONG).show()
            return
        }
        val contentValues = ContentValues()
        contentValues.put(ReceiptsApi.Positions.ROW_PRICE, product.price.toPlainString())
        contentValues.put(ReceiptsApi.Positions.ROW_QUANTITY, 1)
        contentValues.put(ReceiptsApi.Positions.ROW_UUID, product.uuid)

        contentResolver.insert(ReceiptsApi.Positions.URI,
                contentValues
        )
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        return CursorLoader(this, ReceiptsApi.Positions.URI,
                arrayOf(ReceiptsApi.Positions.ROW_NAME),
                null, null, null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mAdapter.swapCursor(null)
    }

    companion object {
        private const val REQUEST_CODE_ADD_COMMODITY = 1
        private const val REQUEST_CODE_PRINT = 2

    }
}
